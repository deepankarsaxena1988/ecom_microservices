package com.ecom.pgvector.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductReindexService {

    private static final Logger log = LoggerFactory.getLogger(ProductReindexService.class);

    private final ProductSyncJob productSyncJob;
    private final ProductVectorUpsertService upsertService;
    private final ProductEmbeddingService embeddingService;

    public ProductReindexService(ProductSyncJob productSyncJob,
                                ProductVectorUpsertService upsertService,
                                ProductEmbeddingService embeddingService) {
        this.productSyncJob = productSyncJob;
        this.upsertService = upsertService;
        this.embeddingService = embeddingService;
    }

    public int rebuildAll() {
        List<ProductTextData> products = productSyncJob.loadProducts();
        int processed = 0;

        for (ProductTextData product : products) {
            if (product == null || product.productId() == null) {
                continue;
            }

            List<Double> embedding = embeddingService.generateEmbedding(product);
            upsertService.upsert(product, embedding);
            processed++;
        }

        log.info("Rebuilt pgvector index for {} products", processed);
        return processed;
    }
}
