package com.ecom.pgvector.search;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductDeltaSyncServiceTest {

    @Test
    void shouldGenerateAStableEmbeddingVectorForProductData() {
        ProductEmbeddingService embeddingService = new ProductEmbeddingService(text -> {
            List<Double> vector = new ArrayList<>(1536);
            for (int i = 0; i < 1536; i++) {
                vector.add(0.25D);
            }
            return vector;
        });

        ProductTextData product = new ProductTextData(
                42L,
                "Noise Cancelling Headphones",
                "Audio gear",
                "Wireless headphones with active noise canceling",
                "Electronics",
                "Audio",
                new BigDecimal("4.9")
        );

        List<Double> embedding = embeddingService.generateEmbedding(product);

        assertEquals(1536, embedding.size());
    }
}
