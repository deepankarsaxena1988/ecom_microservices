package com.ecom.pgvector.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductDeltaSyncService {

    private static final Logger log = LoggerFactory.getLogger(ProductDeltaSyncService.class);

    private final ProductSyncJob productSyncJob;
    private final ProductVectorUpsertService upsertService;
    private final ProductEmbeddingService embeddingService;

    public ProductDeltaSyncService(ProductSyncJob productSyncJob,
                                  ProductVectorUpsertService upsertService,
                                  ProductEmbeddingService embeddingService) {
        this.productSyncJob = productSyncJob;
        this.upsertService = upsertService;
        this.embeddingService = embeddingService;
    }

    public ProductSyncResult syncSince(Long lastProductId) {
        List<ProductTextData> products = productSyncJob.loadProductsSince(lastProductId);

        if (products.isEmpty()) {
            log.info("No new or changed products to sync for productId > {}", lastProductId);
            return new ProductSyncResult(lastProductId, 0);
        }

        Long lastProcessedProductId = null;
        for (ProductTextData product : products) {
            if (product.productId() == null) {
                continue;
            }

            List<Double> embedding = embeddingService.generateEmbedding(product);
            upsertService.upsert(product, embedding);
            lastProcessedProductId = product.productId();
        }

        log.info("Synced {} products to pgvector. Last product id processed: {}", products.size(), lastProcessedProductId);
        return new ProductSyncResult(lastProcessedProductId, products.size());
    }

    public record ProductSyncResult(Long lastProcessedProductId, int processedCount) {
    }
}
