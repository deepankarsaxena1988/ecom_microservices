package com.ecom.pgvector.search;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lets a UI read the active embedding profile and switch it at runtime by
 * profile key only (e.g. "openai" or "qwen"). All connection details
 * (provider/model/baseUrl/apiKey) are resolved from application.yml
 * (app.embedding.profiles.*) - the caller never needs to send them.
 * Changes take effect immediately for reindex, delta sync and search.
 */
@RestController
@RequestMapping("/api/embedding")
public class EmbeddingConfigController {

    private final EmbeddingRuntimeConfig runtimeConfig;

    public EmbeddingConfigController(EmbeddingRuntimeConfig runtimeConfig) {
        this.runtimeConfig = runtimeConfig;
    }

    @GetMapping("/config")
    public ResponseEntity<EmbeddingConfigResponse> getConfig() {
        return ResponseEntity.ok(toResponse());
    }

    @PutMapping("/config")
    public ResponseEntity<?> updateConfig(@RequestBody EmbeddingConfigRequest request) {
        if (!StringUtils.hasText(request.getProfile())) {
            return ResponseEntity.badRequest().body("profile is required");
        }
        if (!runtimeConfig.hasProfile(request.getProfile())) {
            return ResponseEntity.badRequest().body(
                    "Unknown profile '" + request.getProfile() + "'. Available: " + runtimeConfig.getAvailableProfiles());
        }

        runtimeConfig.switchProfile(request.getProfile());
        return ResponseEntity.ok(toResponse());
    }

    private EmbeddingConfigResponse toResponse() {
        return new EmbeddingConfigResponse(
                runtimeConfig.getActiveProfileKey(),
                runtimeConfig.getProvider(),
                runtimeConfig.getModel(),
                runtimeConfig.getBaseUrl(),
                StringUtils.hasText(runtimeConfig.getApiKey()),
                runtimeConfig.getAvailableProfiles()
        );
    }
}

