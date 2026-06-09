# PEP SDK — Design Q&A

This document is structured around 14 design questions, ordered from foundational (architecture, catalog) to compile-time concerns (validation, metadata generation) to runtime concerns (listeners, transactions, request construction). Each section poses a question, gives a concrete answer, and explains the reasoning where a choice is non-obvious.

This doc supersedes the previous `pep-sdk-design.md` where they conflict. Notable extensions to the previous design, called out as they appear:

- The catalog (`pap-endpoints.json`) gains per-attribute **type** and **wire location** fields, enabling datatype validation (Q6) and diverse endpoint shapes (Q4).
- A global default communication mode (`pap.sdk.mode`) is reintroduced as the fallback when an entity does not declare a mode for an operation (Q9).

---

## 1. SDK architecture overview

> **Q1:** What is the SDK architecture, briefly explain each part.

The SDK runs in two phases — **compile time** and **runtime** — both reading the same bundled PAP catalog.

```
┌────────────────────────────── COMPILE TIME ──────────────────────────────┐
│  @PapEntity sources ──► PapAnnotationProcessor ──► metadata.json (per   │
│         ▲                       │                                module) │
│         │             pap-endpoints.json (catalog)                       │
│         └── compile errors                                               │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────── RUNTIME ─────────────────────────────────┐
│  metadata.json ──► MetadataLoader ──► PapEntityRegistry                  │
│  pap-endpoints.json ──► PapCatalog ──► EndpointResolver                  │
│                                                                          │
│  Hibernate ──► PapEntityListener ──► ChangeBuffer (txn-scoped)           │
│                          │           registers ChangeSynchronization     │
│                          ▼                                               │
│           PapTransactionSynchronization (beforeCommit)                   │
│              │                                                           │
│      ┌───────┴───────┐                                                   │
│      ▼               ▼                                                   │
│  PapRequestBuilder   OutboxAppender ──► STL_PEP_OUTBOX                   │
│      │                                       ▲                           │
│      ▼                                       │                           │
│  PapClient (Resilience4j) ◄── PapOutboxConsumer (@Scheduled)             │
│      │                                                                   │
│      ▼                                                                   │
│    PAP                                                                   │
└──────────────────────────────────────────────────────────────────────────┘
```

**Compile-time parts:**

- **`PapAnnotationProcessor`** — runs during `mvn compile`. Activated by `@PapEntity`. Validates each declaration against the catalog. Emits compile errors via `Messager` and writes one `metadata.json` per module via `Filer`.
- **`pap-endpoints.json`** — the catalog. Bundled with the SDK. Names every PAP entity the SDK knows about, the endpoint paths per operation, and the attribute schema (name, type, required, wire location).

**Runtime parts:**

- **`PapCatalog`** — reads `pap-endpoints.json` once at startup. Single source of truth for known PAP types and their endpoint contracts.
- **`MetadataLoader`** — reads every `META-INF/pep-sdk/metadata.json` from the classpath, parses each, and builds `PapEntityDescriptor` instances.
- **`PapEntityRegistry`** — immutable map `Class<?> → PapEntityDescriptor`, plus secondary index by `papEntity` name.
- **`PapEntityListener`** — one Hibernate post-event listener attached to all `@PapEntity` classes. Captures CREATE/UPDATE/DELETE.
- **`ChangeBuffer`** — transaction-scoped accumulator. Coalesces multiple touches of the same entity within one transaction.
- **`PapTransactionSynchronization`** — registered once per transaction the first time the listener captures something. At `beforeCommit` it drains the buffer.
- **`PapRequestBuilder`** — turns a captured change + its descriptor into a `PapRequest` (path, headers, payload, path variables, query params).
- **`PapClient`** — the HTTP boundary. Wraps Spring `RestClient` with Resilience4j retry and circuit breaker.
- **`STL_PEP_OUTBOX`** — durable queue for ASYNC operations. Holds the fully-built request shape.
- **`PapOutboxConsumer`** — scheduled poller. Claims PENDING rows via `FOR UPDATE SKIP LOCKED`, dispatches through `PapClient`, transitions row state.

**Why this split.** Validation done at compile time fails fast in the developer's IDE/build, not in production startup. The metadata file removes runtime classpath scanning, which is slow and fragile across packaging models (fat jar, WAR, layered jar). The catalog being authoritative for URLs prevents drift between service teams.

---

## 2. What is `PapCatalog`?

> **Q2:** What is `PapCatalog`? What does it do? How does it do it?

`PapCatalog` is the runtime representation of `pap-endpoints.json`. It is the single in-memory authority on what PAP types exist and how to talk to them.

**Responsibilities:**

- Load `META-INF/pep-sdk/pap-endpoints.json` once, lazily.
- Tell callers whether a `papEntity` is known.
- Return the `EndpointSpec` (paths per operation, attribute schema) for a given `papEntity`.
- Return the list of required attributes for a given `papEntity` (used both by the processor and by the metadata loader to cross-check).

**Class shape:**

```java
public final class PapCatalog {
    public Set<String> knownEntities();
    public EndpointSpec endpointFor(String papEntity);
    public boolean supports(String papEntity);
}

public record EndpointSpec(
    String createPath,
    String updatePath,
    String deletePath,
    Map<String, AttributeSpec> attributes
) { }

public record AttributeSpec(
    String name,
    PapType type,            // STRING, INTEGER, LONG, BOOLEAN, DECIMAL, TIMESTAMP, UUID
    boolean required,
    WireLocation location    // PAYLOAD, HEADER, PATH, QUERY
) { }
```

**How it loads.** Standard classpath resource access:

```java
try (var in = getClass().getClassLoader().getResourceAsStream("META-INF/pep-sdk/pap-endpoints.json")) {
    if (in == null) throw new PapSdkException("pap-endpoints.json not found on classpath");
    this.catalog = JSON_READER.readValue(in, CatalogDocument.class);
}
```

The same JSON is also read by the annotation processor — from `sdk-processor`'s classpath. Both modules ship with the same file content, kept in lock-step at SDK release time.

**Singleton at runtime.** `PapCatalog` is a single bean, eagerly initialized. Catalog-related lookups are O(1) hash-map reads, so the cost is paid once.

**Why a separate class rather than inlining lookups everywhere.** Three callers need this data — the processor (compile-time), the metadata loader (startup), and the request builder (per-request). Centralizing avoids three slightly-different parsers, and the loader exists in one of the two places where the JSON document lives in code (processor reads its own copy at compile time, runtime reads via `PapCatalog`).

---

## 3. `pap-endpoints.json` — what, where, structure

> **Q3:** What is this catalog (`pap-endpoints.json`), where does it sit, what data does it hold, what is its structure?

**What it is.** A static JSON document describing every PAP entity type the SDK knows how to talk to. Authored by whoever maintains the SDK; never modified by the consuming service.

**Where it sits.** Two locations, identical content:

- `sdk-core/src/main/resources/META-INF/pep-sdk/pap-endpoints.json` — used by `PapCatalog` at runtime.
- `sdk-processor/src/main/resources/META-INF/pep-sdk/pap-endpoints.json` — used by the annotation processor at compile time.

The two copies are kept in sync by the SDK's own build (a Maven `resources` plugin step copies one to the other, or both reference a shared module). The consuming service never touches either file.

**Structure:**

```json
{
  "version": 1,
  "entities": {
    "ResourceInstance": {
      "createPath": "/api/v1/tenants/{tenant_id}/resources",
      "updatePath": "/api/v1/tenants/{tenant_id}/resources/{id}",
      "deletePath": "/api/v1/tenants/{tenant_id}/resources/{id}",
      "attributes": {
        "id":          { "type": "STRING",  "required": true,  "location": "PATH" },
        "tenant_id":   { "type": "STRING",  "required": true,  "location": "PATH" },
        "code":        { "type": "STRING",  "required": true,  "location": "PAYLOAD" },
        "name":        { "type": "STRING",  "required": true,  "location": "PAYLOAD" },
        "description": { "type": "STRING",  "required": false, "location": "PAYLOAD" },
        "topic_id":    { "type": "STRING",  "required": false, "location": "HEADER" },
        "active":      { "type": "BOOLEAN", "required": false, "location": "QUERY" }
      }
    }
  }
}
```

**Field reference:**

| Field | Type | Description |
|---|---|---|
| `version` | integer | Schema version for forward compatibility. |
| `entities` | object | Map of PAP entity name → endpoint definition. |
| `createPath`, `updatePath`, `deletePath` | string | URL templates with `{name}` placeholders that bind to `PATH`-located attributes. |
| `attributes` | object | Map of attribute name → `AttributeSpec`. |
| `attributes[*].type` | enum | PAP-side type. Used for compile-time datatype validation. |
| `attributes[*].required` | boolean | Whether the SDK must have a value for this attribute. Required missing → compile error. |
| `attributes[*].location` | enum | Where the value goes on the wire. `PAYLOAD` (request body), `HEADER`, `PATH`, or `QUERY`. |

**Why static, not configurable.** The endpoints are part of the PAP's published contract. If a service team could override a path, they could silently route to the wrong endpoint. Locking URLs to the SDK forces a deliberate SDK version bump when the PAP changes its API.

---

## 4. Diverse endpoint shapes — headers, path variables, query params, payload

> **Q4:** Each endpoint can have a different set of attributes — some need headers, some need path variables, some need both, plus query / request params. How can we handle all this?

**Answer.** The catalog declares the **wire location** for each attribute. The SDK reads the catalog when building a request and places each attribute's value where the catalog says it goes. The developer just provides values — by `@PapAttribute` on a field for per-instance values, or by `@PapProperty` on `@PapEntity` for per-class static values.

### 4.1 Wire locations

| `WireLocation` | What the SDK does |
|---|---|
| `PAYLOAD` | Adds the `attributeName: value` pair to the JSON request body. |
| `HEADER` | Adds `attributeName: value` as an HTTP header. |
| `PATH` | Substitutes `{attributeName}` in the URL template. |
| `QUERY` | Appends `?attributeName=value` to the URL. |

### 4.2 Value sources

For each attribute the catalog declares for a PAP entity:

1. **From a `@PapAttribute` field** (per-instance): the field's value is read reflectively at capture time.
2. **From a `@PapProperty` on `@PapEntity`** (per-class static): the property's value is used as-is.

The processor checks at compile time that every catalog attribute is covered by exactly one source. Coverage rules:

- A `@PapAttribute(attributeName = X)` on a field covers attribute `X`.
- A `@PapProperty(key = X)` on `@PapEntity` covers attribute `X`.
- If an attribute is declared `required: true` in the catalog and is not covered: compile error.
- If both a field and a property cover the same name: compile error (ambiguous).

### 4.3 Example

For the `ResourceInstance` catalog entry above, a valid mapping could be:

```java
@Entity
@PapEntity(
    entity = "ResourceInstance",
    properties = {
        @PapProperty(key = "tenant_id", value = "1"),   // static -> PATH
        @PapProperty(key = "topic_id",  value = "POLICY") // static -> HEADER
    },
    operationModes = { /* ... */ }
)
public class Pipeline {

    @Id
    @PapAttribute(attributeName = "id")        // per-instance -> PATH
    private UUID id;

    @PapAttribute(attributeName = "code")       // per-instance -> PAYLOAD
    private String code;

    @PapAttribute(attributeName = "name")       // per-instance -> PAYLOAD
    private String name;

    @PapAttribute(attributeName = "description") // per-instance -> PAYLOAD (optional)
    private String description;

    @PapAttribute(attributeName = "active")     // per-instance -> QUERY
    private boolean active;
}
```

At runtime, an UPDATE on a `Pipeline` produces:

```
PATCH /api/v1/tenants/1/resources/8e2a...?active=true
topic_id: POLICY
Content-Type: application/json

{ "code": "PIPE-001", "name": "Build pipeline", "description": "..." }
```

### 4.4 Justification

This design decouples **what** the developer provides from **where** it goes on the wire. The same `@PapAttribute` annotation works whether the value ends up in the body, a header, the path, or the query string — the catalog decides. This means:

- Future endpoints with new shapes don't require new annotations.
- The PAP API evolves through catalog updates, not annotation extensions.
- The annotation surface stays minimal (just two annotations + two helpers).

Alternative considered: putting `location = HEADER` directly on `@PapProperty` and `@PapAttribute`. Rejected because the wire location is a PAP-side concern, not a service-side one. If the PAP moved an attribute from a query param to a header, every service would need to update its annotations. Centralizing the routing in the catalog is the right authority placement.

---

## 5. Compile-time validation — flow and components

> **Q5:** At compile time, how does the processor validate the `@PapEntity`? What is its flow, what are the components involved, how does it work? How do we identify required attributes?

### 5.1 Flow

```
javac (or IDE incremental compiler)
   │
   ├─ discovers PapAnnotationProcessor via META-INF/services
   │
   ├─ for each compilation round:
   │     │
   │     ├─ collect every TypeElement annotated with @PapEntity
   │     │
   │     ├─ for each such TypeElement:
   │     │     │
   │     │     ├─ PapValidator.validate(typeElement, catalog)
   │     │     │     │
   │     │     │     ├─ check 'entity' is in catalog ─── error if absent
   │     │     │     ├─ check exactly one @Id field ──── error if 0 or >1
   │     │     │     ├─ collect @PapAttribute fields and @PapProperty entries
   │     │     │     ├─ for each catalog attribute marked required:
   │     │     │     │     verify it is covered by exactly one source
   │     │     │     ├─ for each @PapAttribute field:
   │     │     │     │     check field type matches catalog's declared type
   │     │     │     ├─ check operationModes has no duplicate operations
   │     │     │     └─ check properties has no duplicate keys
   │     │     │
   │     │     └─ if no errors: build EntityDescriptor and add to collector
   │     │
   │     └─ on processingOver(): MetadataWriter.flush() → metadata.json
   │
   └─ continues compilation (compile errors block class generation)
```

### 5.2 Components

| Class | Responsibility |
|---|---|
| `PapAnnotationProcessor` | The processor entry point. Extends `AbstractProcessor`. Coordinates rounds. |
| `CatalogReader` | Loads `pap-endpoints.json` from the processor's own classpath once per JVM. |
| `PapValidator` | Stateless validation logic. Returns a list of `Diagnostic`s. |
| `TypeMatcher` | Maps PAP types to acceptable Java types (table below). |
| `MetadataWriter` | Accumulates valid descriptors; writes JSON via `Filer` on `processingOver()`. |
| `Messager` (JDK) | Where errors and warnings are reported. The IDE picks these up source-location-aware. |

### 5.3 Identifying required attributes

The catalog is authoritative. Each attribute in the catalog has `required: true|false`. The processor walks the catalog's `required: true` entries and checks each is covered by:

- a `@PapAttribute(attributeName = X)` on some field of the entity class, or
- a `@PapProperty(key = X)` inside the entity's `@PapEntity.properties`.

The error message names exactly what's missing:

```
[ERROR] Pipeline.java:12 — required attribute 'code' has no mapping
        (catalog declares 'code' as required, location PAYLOAD)
        add @PapAttribute(attributeName = "code") to a field,
        or @PapProperty(key = "code", ...) to the @PapEntity
```

### 5.4 Why compile-time

Three reasons:

1. **Fail in the developer's IDE**, not at runtime. Every missing-attribute or wrong-type problem is caught the moment the developer saves the file, with a marker on the offending line.
2. **No runtime catalog round-trip**. The validated descriptors are written to `metadata.json` once. Startup reads the prebuilt descriptors; it doesn't re-validate against the catalog.
3. **No reflection at validation time**. The processor uses `javax.lang.model` — a compile-time model that doesn't load classes. This means the processor doesn't pull in any JPA, Spring, or service dependencies, and it works in incremental compilation.

---

## 6. Verifying `@PapAttribute` presence and datatype

> **Q6:** How will the SDK verify the `@PapAttribute` mentioned on entity attributes are actually present and are of the same datatype for the specified `@PapEntity` 'entity'?

This is performed entirely at compile time by `PapValidator.validate(...)`. Two checks:

### 6.1 Presence check

Already covered in Q5.3 — every catalog attribute marked `required: true` must be covered by either a `@PapAttribute` field or a `@PapProperty`. The presence check is "for each required catalog attribute X, find a covering source."

A second check covers the reverse: every `@PapAttribute(attributeName = X)` field references an `X` that exists in the catalog for this PAP entity. If `X` is not in the catalog, the processor emits a **warning** (not error) — the field will be silently ignored at runtime. This catches typos but does not block the build.

### 6.2 Datatype check

For each `@PapAttribute` field, the processor compares the field's Java type (obtained from `VariableElement.asType()` as a `TypeMirror`) against the catalog's declared PAP type for that attribute, using a fixed compatibility table:

| PAP type | Acceptable Java types |
|---|---|
| `STRING` | `String`, `java.util.UUID`, any enum |
| `INTEGER` | `int`, `Integer`, `short`, `Short`, `byte`, `Byte` |
| `LONG` | `long`, `Long`, `int`, `Integer` |
| `BOOLEAN` | `boolean`, `Boolean` |
| `DECIMAL` | `BigDecimal`, `double`, `Double`, `float`, `Float` |
| `TIMESTAMP` | `java.time.Instant`, `OffsetDateTime`, `ZonedDateTime`, `LocalDateTime`, `java.util.Date`, `java.sql.Timestamp` |
| `UUID` | `java.util.UUID`, `String` |

Type comparison uses fully-qualified names from the `TypeMirror`. No classloading happens — the processor sees only the compile-time symbol table.

Sample failure:

```
[ERROR] Pipeline.java:18 — field 'code' of type java.lang.Integer
        cannot map to PAP attribute 'code' of type STRING
        (allowed Java types for STRING: String, UUID, enum)
```

### 6.3 What about `@PapProperty`?

Properties are always strings (annotation constraint). For each property whose key matches a catalog attribute, the processor checks the catalog's type for that attribute is one that can accept a string-encoded value:

- `STRING`, `UUID` — accept directly.
- `INTEGER`, `LONG`, `DECIMAL`, `BOOLEAN`, `TIMESTAMP` — accept; the runtime will parse the string. If the static value is malformed, the processor reports an error (e.g. `@PapProperty(key = "active", value = "yes")` against a `BOOLEAN` type).

### 6.4 Why types in the catalog at all

The previous design had `requiredAttributes: ["id", "code", "name"]` — names only. That was enough to catch missing attributes but couldn't catch wrong types. A field declared `Integer code` mapping to a PAP STRING would compile fine, then fail at runtime with an opaque serialization error.

Adding `type` per attribute is small and pays for itself the first time a developer changes a field's type without noticing the PAP contract still expects a string.

---

## 7. What lives in `metadata.json`, who writes it, what it looks like

> **Q7:** What is present inside `META-INF/pep-sdk/metadata.json`, who populates that data, how does it look?

**Who writes it.** `MetadataWriter` inside the annotation processor. One file per module, written to the module's `target/classes/META-INF/pep-sdk/metadata.json` via `Filer.createResource(StandardLocation.CLASS_OUTPUT, ...)`.

**Why one file per module.** Each Maven module that contains `@PapEntity` classes runs the processor independently. Each writes its own metadata file. At runtime, every metadata file on the classpath is loaded and merged into the registry. This works whether the consuming app is a single jar or has many internal modules.

**What it contains.** The pre-resolved descriptors — everything `PapEntityRegistry` needs at startup, no further catalog lookups required (other than for path templates).

```json
{
  "version": 1,
  "entities": [
    {
      "entityClass": "com.example.app.Pipeline",
      "papEntity": "ResourceInstance",
      "idField": "id",
      "operationModes": {
        "CREATE": "SYNC",
        "UPDATE": "SYNC",
        "DELETE": "ASYNC"
      },
      "properties": {
        "tenant_id": "1",
        "topic_id":  "POLICY"
      },
      "attributes": [
        { "fieldName": "id",          "attributeName": "id" },
        { "fieldName": "code",        "attributeName": "code" },
        { "fieldName": "name",        "attributeName": "name" },
        { "fieldName": "description", "attributeName": "description" },
        { "fieldName": "active",      "attributeName": "active" }
      ]
    }
  ]
}
```

**Field reference:**

| Field | Description |
|---|---|
| `entityClass` | Fully-qualified Java class name. Loaded via `Class.forName` at startup. |
| `papEntity` | The PAP type. Used to look up endpoints in the catalog. |
| `idField` | Name of the Java field that holds the entity identity. Used to extract the entity id at capture time. |
| `operationModes` | Map of `Operation → CommunicationMode`. Operations not listed here fall back to the global default (see Q9). |
| `properties` | The static key-value pairs from `@PapEntity.properties`, resolved to a flat map. |
| `attributes` | List of `(fieldName, attributeName)` pairs, one per `@PapAttribute`-annotated field. |

**What is NOT in this file.** Anything that comes from the catalog — wire locations, types, required flags, paths. The runtime joins the metadata with the catalog at startup; storing catalog data in the metadata file would duplicate the source of truth and risk drift.

**Justification.** The metadata file is the bridge between compile-time validation and runtime registration. By validating once at compile time and serializing the result, we eliminate runtime classpath scanning (slow, fragile under packaging variations) and runtime re-validation (would require the PAP catalog at startup, possibly a network call if the catalog ever moved to a remote source).

---

## 8. `application.yml` — what the developer fills in, structure

> **Q8:** How do we identify the data that needs to be filled into `application.yml`, what is the structure for it?

The SDK exposes one configuration namespace: `pap.sdk`. Bound to `PapSdkProperties` via Spring Boot's `@ConfigurationProperties`.

```yaml
pap:
  sdk:
    enabled: true                       # master switch; default true
    base-url: https://pap.example.com   # required
    mode: SYNC                          # global default communication mode (see Q9)

    retry:
      max-attempts: 3
      initial-backoff: 200ms
      max-backoff: 5s

    circuit-breaker:
      failure-rate-threshold: 50
      sliding-window-size: 20
      wait-duration-in-open-state: 30s

    timeout:
      connect: 2s
      read: 10s

    outbox:
      poll-interval: 1s
      batch-size: 50
      max-attempts: 10
      worker-pool-size: 4
```

**Required:** `base-url`. Application context fails to start without it (and with a clear message).

**Everything else has defaults**, so a minimal config is two lines:

```yaml
pap:
  sdk:
    base-url: https://pap.example.com
```

**Full property reference:**

| Property | Type | Default | Description |
|---|---|---|---|
| `enabled` | boolean | `true` | When false: no listener registered, no consumer started. |
| `base-url` | URI | — | PAP base URL. Required. |
| `mode` | enum | `SYNC` | Global default mode (Q9). |
| `retry.max-attempts` | int | `3` | Total attempts including the first. |
| `retry.initial-backoff` | Duration | `200ms` | First retry delay. |
| `retry.max-backoff` | Duration | `5s` | Cap on exponential backoff. |
| `circuit-breaker.failure-rate-threshold` | int (%) | `50` | Opens when exceeded. |
| `circuit-breaker.sliding-window-size` | int | `20` | Recent calls considered. |
| `circuit-breaker.wait-duration-in-open-state` | Duration | `30s` | Before half-open. |
| `timeout.connect` | Duration | `2s` | TCP connect timeout. |
| `timeout.read` | Duration | `10s` | HTTP read timeout. |
| `outbox.poll-interval` | Duration | `1s` | Consumer tick interval. |
| `outbox.batch-size` | int | `50` | Max rows claimed per tick. |
| `outbox.max-attempts` | int | `10` | Before DEAD_LETTER. |
| `outbox.worker-pool-size` | int | `4` | Parallel dispatcher workers per JVM. |

**Spring Boot tooling.** `PapSdkProperties` is annotated with `@ConfigurationProperties("pap.sdk")` and the SDK ships `spring-configuration-metadata.json` describing all properties, so developers get auto-completion and validation in IDEs that support Spring metadata.

---

## 9. Default communication mode in `application.yml`

> **Q9:** There must be a provision to set the default communication mode in `application.yml`, that can be used globally if no communication mode is specified.

**Answer.** `pap.sdk.mode` is the global default. Resolution order, most specific first:

1. **Per-operation declared mode** on the entity, from `@PapEntity.operationModes`. If the operation is listed here, this wins.
2. **Global default** from `pap.sdk.mode` in `application.yml`. Used when the entity does not declare a mode for the operation.
3. **Hardcoded fallback** `SYNC`. Used when `pap.sdk.mode` is unset.

This means a developer can:
- Declare modes per operation per entity for fine-grained control.
- Set `pap.sdk.mode: ASYNC` globally to default everything async, then only declare exceptions on individual entities.
- Leave both unset and get safe-by-default SYNC.

**Implementation.** `PapEntityDescriptor.modeFor(Operation op)`:

```java
public CommunicationMode modeFor(Operation op) {
    CommunicationMode declared = operationModes.get(op);
    if (declared != null) return declared;
    return globalDefaultMode;   // injected from PapSdkProperties.mode at descriptor build time
}
```

The global default is set on each descriptor when the registry is built, so the descriptor is fully self-contained at runtime — no lookup back to properties.

### 9.1 Why this is back

The previous iteration removed `pap.sdk.mode` because mode was supposed to be entity-driven. In practice, an operations team often wants a deployment-wide policy ("we're switching to async for everything during the migration") without redeploying service code. The global default gives them that switch without compromising the per-entity model.

---

## 10. Authentication and `PapRequestDecorator`

> **Q10:** How does authentication come into play here? Why do we need `PapRequestDecorator`? It seems redundant — couldn't the SDK be built without it?

The SDK ships no built-in authentication. `PapRequestDecorator` is the integration point for it.

```java
@FunctionalInterface
public interface PapRequestDecorator {
    void decorate(HttpHeaders headers, PapRequest request);
}
```

Multiple decorator beans compose in declared order. Each is called once per outbound request, just before the HTTP call.

### 10.1 Why the SPI is not redundant

The SDK could hard-code one auth mechanism — say, bearer-token from a header named `Authorization`. Three problems:

1. **Auth is per deployment, not per SDK.** One installation uses OAuth2 bearer tokens; another uses mTLS; another uses HMAC request signing; another adds a custom `X-Service-Identity` header. Hard-coding any of these forces the others to fork the SDK.
2. **Auth changes more often than SDK releases.** A token provider library upgrade, a key rotation policy change, or a switch from short-lived to long-lived credentials should not require an SDK release.
3. **Non-auth concerns also need request decoration.** Tracing (`traceparent`), correlation IDs (`X-Correlation-Id`), tenant context overrides for system-initiated calls. All of these are exactly the same shape as auth: read external state, set a header.

The SPI is the minimum surface that lets all of these be plugged in without the SDK knowing.

### 10.2 What the developer does

```java
@Bean
PapRequestDecorator authDecorator(TokenProvider tokens) {
    return (headers, request) -> headers.set("Authorization", "Bearer " + tokens.current());
}

@Bean
PapRequestDecorator tracingDecorator(Tracer tracer) {
    return (headers, request) -> headers.set("traceparent", tracer.currentTraceParent());
}
```

Both beans are picked up by the auto-configuration. They run in order. The developer never touches the SDK's HTTP client.

### 10.3 What if no decorator is provided?

Requests go out with only the SDK's own headers (catalog-declared `HEADER`-location attributes, `Content-Type`). The PAP either accepts unauthenticated requests (in development) or rejects them with 401 — which the SDK surfaces as `PapRejectedException`.

### 10.4 Could it be removed?

Only if you're willing to fix the auth mechanism in the SDK itself. Given that the SDK is used by multiple services with potentially different security postures, the SPI is the more conservative choice. It costs one interface (six lines) and a wiring step in the auto-config. The tradeoff is favorable.

---

## 11. Entity listeners — how registered, how many, dynamic or not

> **Q11:** Explain how the entity listeners will be used, how they are registered, when they are registered, how many exist, are they dynamically created, etc.

### 11.1 How many

**Exactly one** `PapEntityListener` instance per Spring context. It's a singleton bean. It is **not** generated per entity — one generic listener handles every `@PapEntity` class.

### 11.2 What it implements

`PapEntityListener` implements three Hibernate post-event interfaces:

```java
public final class PapEntityListener
        implements PostInsertEventListener,
                   PostUpdateEventListener,
                   PostDeleteEventListener {

    @Override public void onPostInsert(PostInsertEvent e) { capture(e.getEntity(), CREATE); }
    @Override public void onPostUpdate(PostUpdateEvent e) { capture(e.getEntity(), UPDATE); }
    @Override public void onPostDelete(PostDeleteEvent e) { capture(e.getEntity(), DELETE); }

    @Override public boolean requiresPostCommitHandling(EntityPersister p) { return false; }
}
```

### 11.3 How it's registered

By a small bean called `HibernateListenerRegistrar` that runs `@PostConstruct` after Spring has built the `EntityManagerFactory`:

```java
@PostConstruct
public void register() {
    SessionFactoryImplementor sfi = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
    EventListenerRegistry registry = sfi.getServiceRegistry().getService(EventListenerRegistry.class);
    registry.appendListeners(EventType.POST_INSERT, listener);
    registry.appendListeners(EventType.POST_UPDATE, listener);
    registry.appendListeners(EventType.POST_DELETE, listener);
}
```

**Why not the `META-INF/services` Integrator approach.** The Integrator runs before Spring exists, so it would need a static reference to the listener instance — ugly and racy in tests. Registering via `@PostConstruct` after the EMF is built is cleaner, avoids static state, and produces identical runtime behavior because event listeners fire on entity operations, not during metadata bootstrapping.

### 11.4 Why one listener for all entities

A single listener with internal type-checking is simpler than per-entity listeners for three reasons:

1. **No dynamic class generation.** Per-entity listeners would mean either annotation processing emitting Java sources for each entity (compile-time codegen complexity) or bytecode generation at runtime (CGLIB-style). Both add moving parts; both make debugging worse.
2. **The work is the same.** Each event handler reads `event.getEntity()`, looks up the descriptor, builds a `PapEntityChange`. Splitting that into N classes adds dispatch overhead and gains nothing.
3. **The registry is already class-keyed.** `PapEntityRegistry.find(entity.getClass())` returns the right descriptor in O(1). There's no scenario where having a per-class listener would be faster.

### 11.5 When it's registered

Once, at application startup, in `HibernateListenerRegistrar.@PostConstruct`. After that, every entity event flows through it until the application shuts down. The listener is stateless across requests — all per-transaction state lives in `ChangeBuffer` bound to `TransactionSynchronizationManager`.

---

## 12. Transaction interception and mode resolution

> **Q12:** Explain how the transaction will be intercepted/listened to, how the entity lifecycle will be observed, and how appropriate modes will be identified.

### 12.1 The two layers

The SDK uses two cooperating Spring/Hibernate hooks:

| Layer | What it does | Why |
|---|---|---|
| **Hibernate event listener** | Fires on each per-entity persist/update/delete event during the transaction. Captures *what changed*. | Hibernate is the only place that sees individual entity state transitions; Spring's transaction hooks don't have access to entity-level events. |
| **Spring `TransactionSynchronization`** | Fires once at `beforeCommit` (and `afterCompletion`). Dispatches *what was accumulated*. | This is the place where we know the transaction is about to commit successfully — the right moment to call the PAP (SYNC) or write the outbox (ASYNC). |

The listener captures; the synchronization dispatches. They communicate through the `ChangeBuffer`.

### 12.2 The lifecycle, step by step

1. Service code calls `repository.save(entity)`. Spring's `@Transactional` ensures a transaction is active.
2. Hibernate flushes the change (now or at commit). It fires a `PostInsertEvent` / `PostUpdateEvent` / `PostDeleteEvent`.
3. `PapEntityListener` receives the event:
   - Looks up `PapEntityDescriptor` from `PapEntityRegistry`.
   - **Resolves the mode** via `descriptor.modeFor(operation)` — uses the per-operation declaration, falling back to the global default (see Q9).
   - Reads the entity's id and snapshots its `@PapAttribute` fields into a `Map<String,Object>`.
   - Builds a `PapEntityChange(entityClass, entityId, operation, mode, snapshot)`.
4. The listener appends the change to the transaction-scoped `ChangeBuffer`.
   - If no buffer exists yet for this transaction: create one, bind it to `TransactionSynchronizationManager`, and register `PapTransactionSynchronization`.
   - Otherwise: append (with coalescing).
5. Steps 2–4 may repeat many times within the transaction.
6. Spring is about to commit. `PapTransactionSynchronization.beforeCommit(readOnly=false)` runs.
7. The synchronization drains the buffer. For each net change:
   - SYNC: build a `PapRequest` via `PapRequestBuilder`; call `PapClient.send(...)`. A thrown exception fails the synchronization, which fails the commit, which rolls back the transaction.
   - ASYNC: call `OutboxAppender.append(change)`. The outbox `INSERT` runs in the same transaction as the entity changes, so they commit atomically.
8. Spring commits the transaction.
9. `afterCompletion(int status)` runs. The synchronization unbinds the buffer regardless of outcome.

### 12.3 Why mode is resolved at capture, not at dispatch

Per-operation modes on `@PapEntity` are static — they depend on `(entityClass, operation)`, both of which are known at capture time. Resolving early and storing the resolved mode on `PapEntityChange` keeps the dispatch loop trivial: "look at `change.mode()`, branch." No registry lookups during the time-sensitive `beforeCommit` callback.

It also keeps the code testable: `ChangeBuffer` and `PapTransactionSynchronization` can be unit-tested with synthetic `PapEntityChange` values, without needing a full registry or properties bean.

### 12.4 What about coalescing modes

If the buffer ends up coalescing two changes for the same key — say CREATE then UPDATE → net CREATE — the **net operation's mode** (from the descriptor) wins. Not "the mode of the first change" or "if any was SYNC then SYNC." The mode for CREATE is whatever `createMode` says, regardless of what the intermediate UPDATE's mode would have been.

This keeps the per-operation contract honored: if `createMode = SYNC`, every CREATE goes SYNC; the user's contract for that operation is preserved.

---

## 13. Why `ChangeBuffer` is required

> **Q13:** Why can't the SDK be built without a `ChangeBuffer`? What is its purpose, how does it help, can it be removed?

### 13.1 What it does

Per-transaction accumulator. Stores at most one `PapEntityChange` per `(entityClass, entityId)` key, applying coalescing rules when a new change arrives for an existing key. Drained once at `beforeCommit`.

### 13.2 What breaks without it

Two real problems:

1. **Multiple PAP calls for one logical change.** A service method might `save()` an entity, then call a helper that mutates and `save()`s it again (legitimate user code). Without buffering, each Hibernate flush triggers a listener event, triggers a PAP call. The PAP sees two writes for one transaction. Possibly different mid-states; possibly the first one is rolled back.
2. **Dispatching state that will never commit.** Hibernate fires `PostUpdate` events during flush, not commit. The transaction may still roll back afterward for an unrelated reason (a constraint violation in a later operation). Without buffering and the `beforeCommit` deferral, we may have already pushed the change to the PAP before knowing the transaction will fail.

The buffer is what makes "one DB transaction, one effective PAP write" possible.

### 13.3 Couldn't we just dispatch from `beforeCommit` directly?

The synchronization runs at `beforeCommit`. The listener runs during flushes. They're different layers because they observe different things — see Q12.1. The buffer is the data structure that joins them: the listener has access to per-entity data but no commit signal; the synchronization has the commit signal but no per-entity data.

Without the buffer you'd need to either:
- Dispatch from the listener (problems above), or
- Use Hibernate's dirty-tracking from inside the synchronization (much more invasive — re-inspecting every dirty entity via `SessionFactoryImplementor` internals at commit time), or
- Use AOP around `@Transactional` methods (forces method-level annotations, which the spec explicitly rejects).

The buffer is the least-coupled option.

### 13.4 Can it be removed if we accept the trade-offs

In principle, a simpler SDK that did **not** support multiple writes per transaction and **always** dispatched on the spot could work without a buffer. It would be wrong for most real services (one tx, many writes is normal in Spring Data) and would dispatch states that may roll back. We keep the buffer.

### 13.5 Implementation footprint

The buffer is ~60 lines of code — a `LinkedHashMap<Key, PapEntityChange>` with a coalescing `append(change)` method. Cheap. The cost of having it is small; the cost of not having it shows up in production as duplicate PAP writes that nobody can explain.

---

## 14. Retrieving the data needed for a request

> **Q14:** Explain how the required data will be retrieved.

The PAP request needs values for every catalog-declared attribute of the entity. The SDK pulls those values from two sources, decided per attribute at startup when the descriptor is built.

### 14.1 The two sources, by catalog attribute

For each attribute the catalog declares for the PAP entity:

| Source | When applicable | How |
|---|---|---|
| **Entity field** | An `@PapAttribute(attributeName = X)` exists on a field. | Read the field reflectively via `AttributeAccessor.read(entityInstance)`. |
| **Static property** | A `@PapProperty(key = X)` exists on `@PapEntity`. | Read the (pre-resolved) value from `descriptor.properties().get(X)`. |

Compile-time validation (Q5, Q6) guarantees exactly one source per attribute, and that required attributes have a source.

### 14.2 The `AttributeAccessor`

Built once at startup per `@PapAttribute` field:

```java
public final class AttributeAccessor {
    private final String attributeName;
    private final Field  field;        // setAccessible(true) once at construction

    public Object read(Object entity) {
        try { return field.get(entity); }
        catch (IllegalAccessException e) { throw new PapSdkException(...); }
    }
}
```

`AttributeAccessor` instances are stored in the `PapEntityDescriptor`. Reading a field is one reflective call per attribute, no per-call lookups, no exceptions in the steady state.

### 14.3 Per-attribute retrieval at capture time

When the listener captures a change, it asks the descriptor to **snapshot** the entity:

```java
public Map<String, Object> snapshot(Object entity) {
    Map<String, Object> out = new LinkedHashMap<>();
    for (AttributeAccessor a : attributes) {
        out.put(a.attributeName(), a.read(entity));
    }
    // Static properties are NOT in the snapshot — they're pulled from the descriptor at build time.
    return out;
}
```

The snapshot is per-instance data; the static properties live separately on the descriptor and are merged in by `PapRequestBuilder` (Q15).

### 14.4 Why split static and per-instance values

- **Lifecycle.** Per-instance values must be read at capture time (the entity may be mutated after). Static values are constants; reading them once at startup is enough.
- **Memory.** Storing the same `tenant_id` string on every `PapEntityChange` for a million Pipeline records would waste space. Keeping it on the descriptor means one copy.
- **Clarity.** The split mirrors what the developer declared — `@PapAttribute` vs `@PapProperty`. The runtime preserves the distinction rather than collapsing it.

### 14.5 What about the `@Id` field

The `idField` (declared in metadata.json, also marked `@Id` on the entity) is read both:
- As part of the snapshot (for the catalog attribute named `id`, if it has `location: PATH` or `location: PAYLOAD`).
- Separately as `change.entityId()`, which is what the buffer uses for keying / coalescing and what the outbox stores as `entity_id` (when that column is added — see Open Issues in the main design doc).

---

## 15. Building the request

> **Q15:** Explain how the request will be built.

`PapRequestBuilder` is stateless. Given a descriptor and a captured change, it produces a `PapRequest`.

### 15.1 Inputs

- `PapEntityDescriptor descriptor` — has `papEntity`, `properties` (static values), id field name.
- `PapEntityChange change` — has `operation`, `entityId`, `snapshot` (per-instance values).
- `PapCatalog catalog` — lookup the `EndpointSpec` for `descriptor.papEntity()`.
- `EndpointResolver` — collaborator for path substitution.

### 15.2 Output

```java
public record PapRequest(
    HttpMethod method,            // POST for CREATE, PATCH for UPDATE, DELETE for DELETE
    String     path,              // URL path after substituting {placeholders}
    Map<String, Object> payload,  // JSON body
    Map<String, String> headers,  // HTTP headers
    Map<String, String> pathVariables,  // for diagnostic / outbox storage
    Map<String, String> requestParams,  // query params
    String     entityType,        // descriptor.papEntity()
    String     entityId,          // change.entityId()
    Operation  operation
) { }
```

### 15.3 Build steps

```
1. Look up EndpointSpec for descriptor.papEntity() in PapCatalog.

2. Build a unified value map keyed by attribute name:
       for each AttributeAccessor in descriptor.attributes():
           values.put(a.attributeName(), change.snapshot().get(a.attributeName()))
       for each (k, v) in descriptor.properties():
           values.put(k, v)

3. For each (attributeName, spec) in EndpointSpec.attributes():
       value = values.get(attributeName)
       skip if value is null AND spec.required() is false
       fail if value is null AND spec.required() is true
       switch (spec.location()):
           PAYLOAD  -> payload.put(attributeName, value)
           HEADER   -> headers.put(attributeName, stringify(value))
           PATH     -> pathVariables.put(attributeName, stringify(value))
           QUERY    -> requestParams.put(attributeName, stringify(value))

4. Resolve the path template:
       template = switch (change.operation()):
           CREATE -> endpoint.createPath()
           UPDATE -> endpoint.updatePath()
           DELETE -> endpoint.deletePath()
       path = EndpointResolver.resolve(template, pathVariables)

5. For DELETE, payload is empty regardless of values.

6. Return new PapRequest(...).
```

### 15.4 Path resolution

`EndpointResolver` substitutes `{name}` placeholders in path templates with the corresponding entry in `pathVariables`. Throws `PapSdkException` if a placeholder has no value (which compile-time validation should have prevented).

```java
public String resolve(String template, Map<String, String> pathVars) {
    String result = template;
    for (var e : pathVars.entrySet()) {
        result = result.replace("{" + e.getKey() + "}", urlEncode(e.getValue()));
    }
    int unfilled = result.indexOf('{');
    if (unfilled >= 0) {
        throw new PapSdkException("unfilled path placeholder: " + result.substring(unfilled));
    }
    return result;
}
```

URL-encoding path segments is required because attribute values may contain reserved characters.

### 15.5 Header assembly

Headers come from three sources, applied in order:

1. Catalog-declared `HEADER`-location attributes (from step 3 above).
2. `Content-Type: application/json` if the operation has a payload.
3. Decorator chain — each `PapRequestDecorator` runs (auth, tracing, etc.).

Later sources override earlier ones for the same key, so a decorator can override a catalog-set header if needed.

### 15.6 The same builder serves SYNC and ASYNC

For SYNC, `PapTransactionSynchronization` calls the builder and hands the result to `PapClient.send(...)`.

For ASYNC, `DefaultOutboxAppender` calls the same builder and persists the four maps (`payload`, `headers`, `pathVariables`, `requestParams`) as JSONB columns in `STL_PEP_OUTBOX`. The outbox consumer later reads those columns, reconstructs an in-memory request (or builds the HTTP call directly from them), and dispatches. Importantly, the consumer **does not re-run the builder** — the request is fully resolved at the point of write, so subsequent state changes to the entity (or to the catalog) don't alter what gets dispatched.

### 15.7 Why a separate builder class

The builder is small (~50 lines) but it earns its keep:

- It's stateless and pure: same inputs → same outputs. Easy to unit-test against a synthetic descriptor.
- It's called from two places (synchronization and outbox appender). Inlining would duplicate the assembly logic and risk drift.
- It's the natural seam to introduce request-shape changes (e.g., bulk endpoints in the future) without touching the listener, the buffer, the synchronization, the client, or the consumer.
