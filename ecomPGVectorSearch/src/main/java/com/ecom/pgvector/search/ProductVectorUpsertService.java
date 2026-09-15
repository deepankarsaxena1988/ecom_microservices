package com.ecom.pgvector.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductVectorUpsertService {

    private static final Logger log = LoggerFactory.getLogger(ProductVectorUpsertService.class);

    private static final String UPSERT_SQL = """
            INSERT INTO product_vector (product_id, category_id, domain, content_text, embedding, source_updated_at)
            VALUES (?, ?, ?, ?, CAST(? AS vector), NOW())
            ON CONFLICT (product_id) DO UPDATE
            SET category_id = EXCLUDED.category_id,
                domain = EXCLUDED.domain,
                content_text = EXCLUDED.content_text,
                embedding = EXCLUDED.embedding,
                source_updated_at = NOW()
            """;

    private final JdbcTemplate jdbcTemplate;

    public ProductVectorUpsertService(@Qualifier("vectorJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsert(ProductTextData product, List<Double> embedding) {
        if (product == null || product.productId() == null || embedding == null || embedding.isEmpty()) {
            log.warn("Skipping vector upsert because product or embedding is missing");
            return;
        }

        String content = new ProductTextTransformer().toEmbeddingText(product);
        String vectorLiteral = buildVectorLiteral(embedding);

        jdbcTemplate.update(
                UPSERT_SQL,
                product.productId(),
                null,
                product.domain(),
                content,
                vectorLiteral
        );
    }

    private String buildVectorLiteral(List<Double> embedding) {
        return "[" + embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }
}
