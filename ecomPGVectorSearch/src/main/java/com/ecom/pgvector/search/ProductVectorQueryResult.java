package com.ecom.pgvector.search;

public record ProductVectorQueryResult(
        Long productId,
        String contentText,
        Double similarity,
        Double distance
) {
}
