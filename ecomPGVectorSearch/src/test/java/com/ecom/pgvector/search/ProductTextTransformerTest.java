package com.ecom.pgvector.search;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTextTransformerTest {

    private final ProductTextTransformer transformer = new ProductTextTransformer();

    @Test
    void shouldBuildStructuredEmbeddingText() {
        ProductTextData product = new ProductTextData(
                101L,
                "Samsung Galaxy  S21   Ultra",
                "Premium flagship phone",
                "5G smartphone with 120Hz display and pro camera features",
                "Electronics",
                "Mobile",
                new BigDecimal("4.8")
        );

        String output = transformer.toEmbeddingText(product);

        assertEquals(
                "Name: Samsung Galaxy S21 Ultra | Heading: Premium flagship phone | Detail: 5G smartphone with 120Hz display and pro camera features | Category: Electronics | Domain: Mobile | Rating: 4.8",
                output
        );
    }

    @Test
    void shouldIgnoreBlankFieldsAndNullValues() {
        ProductTextData product = new ProductTextData(
                202L,
                "   ",
                null,
                "Slim headphone with noise cancellation",
                "",
                "Audio",
                null
        );

        String output = transformer.toEmbeddingText(product);

        assertTrue(output.contains("Detail: Slim headphone with noise cancellation"));
        assertTrue(output.contains("Domain: Audio"));
        assertTrue(!output.contains("Name:"));
        assertTrue(!output.contains("Rating:"));
    }
}
