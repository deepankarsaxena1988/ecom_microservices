package com.ecom.pgvector.search;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductReindexController {

    private final ProductReindexService productReindexService;

    public ProductReindexController(ProductReindexService productReindexService) {
        this.productReindexService = productReindexService;
    }

    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Object>> reindex() {
        int processed = productReindexService.rebuildAll();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "processedCount", processed
        ));
    }
}
