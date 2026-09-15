package com.ecom.pgvector.search;

import java.math.BigDecimal;

public record ProductTextData(
        Long productId,
        String productName,
        String productDescHeading,
        String productDescDetail,
        String categoryName,
        String domain,
        BigDecimal productRating
) {
}
