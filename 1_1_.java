package com.sentinel.pep.sdk.starter.registration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.pep.sdk.starter.properties.SentinelPepProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ServiceRegistrationListener implements ApplicationListener<ApplicationReadyEvent> {

    private final SentinelPepProperties sentinelProps;
    private final PepRegistrationClient pepRegistrationClient;
    private final ObjectMapper objectMapper;

    @Override
    public void onApplicationReady(ApplicationReadyEvent event) {
        try {
            Map<String, Object> registrationPayload = buildUnifiedRegistration();
            if (registrationPayload.isEmpty()) {
                log.warn("No PEP endpoints or registration config found to register.");
                return;
            }
            log.info("Registering service configs with PAP for service: {}", sentinelProps.getServiceCode());

            String jsonPayload = objectMapper.writeValueAsString(registrationPayload);
            pepRegistrationClient.register(jsonPayload);

        } catch (Exception e) {
            log.error("Failed to register service with PAP: {}", e.getMessage(), e);
            if (sentinelProps.getScan() != null && sentinelProps.getScan().isFailOnConfigRegFail()) {
                throw new IllegalStateException("Service registration failed", e);
            }
        }
    }

    public Map<String, Object> buildUnifiedRegistration() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // 1. Resolve version from service-meta.json or SentinelPepProperties
        String version = sentinelProps.getVersion();
        Resource metaRes = resolver.getResource("classpath:META-INF/pep-sdk/service-meta.json");
        if (metaRes.exists()) {
            try (InputStream in = metaRes.getInputStream()) {
                Map<String, String> meta = objectMapper.readValue(in, new TypeReference<>() {});
                if (meta.containsKey("serviceVersion") && !meta.get("serviceVersion").isBlank()) {
                    version = meta.get("serviceVersion");
                }
            }
        }
        if (version == null || version.isBlank()) {
            version = "v1.0.0";
        }

        // 2. Discover all endpoint fragments generated across submodule JARs
        Resource[] fragments = resolver.getResources("classpath*:META-INF/pep-sdk/fragments/*-endpoints.json");
        Map<String, Object> mergedPaths = new LinkedHashMap<>();

        for (Resource fragment : fragments) {
            log.debug("Merging PEP endpoint fragment from: {}", fragment.getFilename());
            try (InputStream in = fragment.getInputStream()) {
                Map<String, Object> paths = objectMapper.readValue(in, new TypeReference<>() {});
                for (Map.Entry<String, Object> entry : paths.entrySet()) {
                    String path = entry.getKey().startsWith("/") ? entry.getKey() : "/" + entry.getKey();
                    mergedPaths.put(path, entry.getValue());
                }
            }
        }

        // 3. Fallback: single-module registration.json (e.g. dummy-service)
        if (mergedPaths.isEmpty()) {
            Resource fullRes = resolver.getResource("classpath:META-INF/pep-sdk/registration.json");
            if (fullRes.exists()) {
                try (InputStream in = fullRes.getInputStream()) {
                    Map<String, Object> fullJson = objectMapper.readValue(in, new TypeReference<>() {});
                    // If fullJson already starts with a version key (e.g. {"v5.0.1": { ... }}), return directly
                    for (String key : fullJson.keySet()) {
                        if (key.matches("^v?\\d+\\.\\d+\\.\\d+.*")) {
                            return fullJson;
                        }
                    }
                    mergedPaths.putAll(fullJson);
                }
            }
        }

        if (mergedPaths.isEmpty()) {
            return Map.of();
        }

        // PAP ServiceConfigDTO expects { "<service_version>": { "<endpoint_path>": { "<METHOD>": ... } } }
        return Map.of(version, mergedPaths);
    }
}
