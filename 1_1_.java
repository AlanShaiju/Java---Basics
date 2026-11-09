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
            log.info("Registering service with PAP using merged definitions: {}", sentinelProps.getServiceCode());

            // Convert map directly into valid JSON string
            String jsonPayload = objectMapper.writeValueAsString(registrationPayload);
            pepRegistrationClient.register(jsonPayload);

        } catch (Exception e) {
            log.error("Failed to register service with PAP: {}", e.getMessage(), e);
            if (sentinelProps.getScan() != null && sentinelProps.getScan().isFailOnConfigRegFail()) {
                throw new IllegalStateException("Service registration failed", e);
            }
        }
    }

    /**
     * Aggregates endpoint mappings directly without the outer version wrapper.
     */
    public Map<String, Object> buildUnifiedRegistration() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // 1. Single-module backward compatibility: check for standalone registration.json
        Resource fullRes = resolver.getResource("classpath:META-INF/pep-sdk/registration.json");
        if (fullRes.exists()) {
            try (InputStream in = fullRes.getInputStream()) {
                Map<String, Object> fullJson = objectMapper.readValue(in, new TypeReference<>() {});
                if (hasEndpoints(fullJson)) {
                    // If fullJson has an outer version key (e.g., {"v5.0.1": { paths... }}), unwrap it
                    for (Map.Entry<String, Object> entry : fullJson.entrySet()) {
                        if (entry.getValue() instanceof Map<?, ?> inner) {
                            return (Map<String, Object>) inner;
                        }
                    }
                    return fullJson;
                }
            }
        }

        // 2. Multi-module mode: aggregate all fragment endpoint definitions
        Resource[] fragments = resolver.getResources("classpath*:META-INF/pep-sdk/fragments/*-endpoints.json");
        Map<String, Object> mergedPaths = new LinkedHashMap<>();

        for (Resource fragment : fragments) {
            log.debug("Merging PEP endpoint fragment from: {}", fragment.getFilename());
            try (InputStream in = fragment.getInputStream()) {
                Map<String, Object> paths = objectMapper.readValue(in, new TypeReference<>() {});
                mergedPaths.putAll(paths);
            }
        }

        return mergedPaths;
    }

    private boolean hasEndpoints(Map<String, Object> json) {
        if (json == null || json.isEmpty()) return false;
        for (Object val : json.values()) {
            if (val instanceof Map<?, ?> m && !m.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
