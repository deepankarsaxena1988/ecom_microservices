package com.ecom.pgvector.search;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Resolves the pgvector column on {@code product_vector} that stores vectors
 * for a given embedding dimension. Since pgvector requires every vector in an
 * indexed column to share the same dimension, each supported embedding model
 * dimension gets its own fixed-size vector column (with its own HNSW index) -
 * see {@code ecomPGVectorDB/init/01_product_vector_schema.sql}:
 * - 1536 -> embedding_1536 (e.g. OpenAI text-embedding-3-small)
 * - 1024 -> embedding_1024 (e.g. Ollama qwen3-embedding:0.6b)
 * <p>
 * Adding support for a new dimension requires adding a new vector column +
 * HNSW index in the schema, then registering it here.
 */
@Component
public class EmbeddingVectorColumnResolver {

    private static final Map<Integer, String> DIMENSION_TO_COLUMN = Map.of(
            1536, "embedding_1536",
            1024, "embedding_1024"
    );

    public String resolveColumn(int dimension) {
        String column = DIMENSION_TO_COLUMN.get(dimension);
        if (column == null) {
            throw new IllegalStateException(
                    "Unsupported embedding dimension " + dimension + ". Supported dimensions: "
                            + DIMENSION_TO_COLUMN.keySet()
                            + ". Add a matching vector column + HNSW index in product_vector and register it in "
                            + "EmbeddingVectorColumnResolver.");
        }
        return column;
    }
}
