package com.ecom.pgvector.search;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductReindexController {

    private final ProductReindexService productReindexService;
    private final EmbeddingRuntimeConfig embeddingRuntimeConfig;

    public ProductReindexController(ProductReindexService productReindexService,
                                     EmbeddingRuntimeConfig embeddingRuntimeConfig) {
        this.productReindexService = productReindexService;
        this.embeddingRuntimeConfig = embeddingRuntimeConfig;
    }

    /**
     * Rebuilds the pgvector index. Optionally accepts a "profile" query param
     * (e.g. "openai" or "qwen") to switch the active embedding profile before
     * reindexing, so all vectors in this run are generated with that model.
     * If omitted, the currently active profile is used.
     */
    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Object>> reindex(
            @RequestParam(value = "profile", required = false) String profile) {
        if (profile != null && !profile.isBlank()) {
            if (!embeddingRuntimeConfig.hasProfile(profile)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Unknown profile '" + profile + "'. Available: "
                                + embeddingRuntimeConfig.getAvailableProfiles()
                ));
            }
            embeddingRuntimeConfig.switchProfile(profile);
        }

        int processed = productReindexService.rebuildAll();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "profile", embeddingRuntimeConfig.getActiveProfileKey(),
                "provider", embeddingRuntimeConfig.getProvider(),
                "model", embeddingRuntimeConfig.getModel(),
                "processedCount", processed
        ));
    }
}

