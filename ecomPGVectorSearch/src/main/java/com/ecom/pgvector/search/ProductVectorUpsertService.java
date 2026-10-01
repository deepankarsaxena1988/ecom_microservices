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

    private static final String UPSERT_SQL_TEMPLATE = """
            INSERT INTO product_vector (product_id, category_id, domain, content_text, %1$s, embedding_provider, embedding_model, source_updated_at)
            VALUES (?, ?, ?, ?, CAST(? AS vector), ?, ?, NOW())
            ON CONFLICT (product_id) DO UPDATE
            SET category_id = EXCLUDED.category_id,
                domain = EXCLUDED.domain,
                content_text = EXCLUDED.content_text,
                %1$s = EXCLUDED.%1$s,
                embedding_provider = EXCLUDED.embedding_provider,
                embedding_model = EXCLUDED.embedding_model,
                source_updated_at = NOW()
            """;

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingRuntimeConfig embeddingRuntimeConfig;
    private final EmbeddingVectorColumnResolver columnResolver;

    public ProductVectorUpsertService(@Qualifier("vectorJdbcTemplate") JdbcTemplate jdbcTemplate,
                                       EmbeddingRuntimeConfig embeddingRuntimeConfig,
                                       EmbeddingVectorColumnResolver columnResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingRuntimeConfig = embeddingRuntimeConfig;
        this.columnResolver = columnResolver;
    }

    public void upsert(ProductTextData product, List<Double> embedding) {
        if (product == null || product.productId() == null || embedding == null || embedding.isEmpty()) {
            log.warn("Skipping vector upsert because product or embedding is missing");
            return;
        }

        String content = new ProductTextTransformer().toEmbeddingText(product);
        String vectorLiteral = buildVectorLiteral(embedding);
        // Only the column matching the active profile's dimension is written;
        // any vector previously stored for a different dimension/profile is left untouched.
        String column = columnResolver.resolveColumn(embeddingRuntimeConfig.getDimension());

        jdbcTemplate.update(
                UPSERT_SQL_TEMPLATE.formatted(column),
                product.productId(),
                null,
                product.domain(),
                content,
                vectorLiteral,
                embeddingRuntimeConfig.getProvider(),
                embeddingRuntimeConfig.getModel()
        );
    }

    private String buildVectorLiteral(List<Double> embedding) {
        return "[" + embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }
}
