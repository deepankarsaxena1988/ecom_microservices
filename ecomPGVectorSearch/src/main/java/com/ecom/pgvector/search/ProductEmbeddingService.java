package com.ecom.pgvector.search;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class ProductEmbeddingService {

    private final EmbeddingModelClient embeddingModelClient;

    public ProductEmbeddingService(EmbeddingModelClient embeddingModelClient) {
        this.embeddingModelClient = embeddingModelClient;
    }

    public List<Double> generateEmbedding(ProductTextData product) {
        if (product == null) {
            return Collections.emptyList();
        }

        ProductTextTransformer transformer = new ProductTextTransformer();
        return generateEmbedding(transformer.toEmbeddingText(product));
    }

    public List<Double> generateEmbedding(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }

        return embeddingModelClient.embed(text);
    }
}
