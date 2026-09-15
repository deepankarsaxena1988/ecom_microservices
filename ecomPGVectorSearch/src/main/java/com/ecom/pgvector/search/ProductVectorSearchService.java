package com.ecom.pgvector.search;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductVectorSearchService {

    private static final String SEARCH_SQL = """
            SELECT product_id,
                   content_text,
                   1 - (embedding <=> CAST(? AS vector)) AS similarity,
                   embedding <=> CAST(? AS vector) AS distance
            FROM product_vector
            WHERE embedding IS NOT NULL
            ORDER BY embedding <=> CAST(? AS vector)
            LIMIT ?
            """;

    private final JdbcTemplate vectorJdbcTemplate;
    private final ProductEmbeddingService productEmbeddingService;
    private final ProductTextTransformer productTextTransformer;

    public ProductVectorSearchService(@org.springframework.beans.factory.annotation.Qualifier("vectorJdbcTemplate") JdbcTemplate vectorJdbcTemplate,
                                     ProductEmbeddingService productEmbeddingService,
                                     ProductTextTransformer productTextTransformer) {
        this.vectorJdbcTemplate = vectorJdbcTemplate;
        this.productEmbeddingService = productEmbeddingService;
        this.productTextTransformer = productTextTransformer;
    }

    public List<ProductVectorQueryResult> search(String queryText, int limit) {
        if (queryText == null || queryText.isBlank()) {
            return List.of();
        }

        String vectorLiteral = buildVectorLiteral(productEmbeddingService.generateEmbedding(queryText));

        return vectorJdbcTemplate.query(
                SEARCH_SQL,
                (rs, rowNum) -> new ProductVectorQueryResult(
                        rs.getLong("product_id"),
                        rs.getString("content_text"),
                        rs.getDouble("similarity"),
                        rs.getDouble("distance")
                ),
                vectorLiteral,
                vectorLiteral,
                vectorLiteral,
                Math.max(1, Math.min(limit, 20))
        );
    }

    private String buildVectorLiteral(List<Double> embedding) {
        return "[" + embedding.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(",")) + "]";
    }
}
