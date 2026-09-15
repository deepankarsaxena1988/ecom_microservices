package com.ecom.pgvector.search;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductTextTransformer {

    public String toEmbeddingText(ProductTextData product) {
        if (product == null) {
            return "";
        }

        List<String> fields = new ArrayList<>();
        appendField(fields, "Name", product.productName());
        appendField(fields, "Heading", product.productDescHeading());
        appendField(fields, "Detail", product.productDescDetail());
        appendField(fields, "Category", product.categoryName());
        appendField(fields, "Domain", product.domain());

        if (product.productRating() != null) {
            fields.add("Rating: " + product.productRating().stripTrailingZeros().toPlainString());
        }

        return normalize(String.join(" | ", fields));
    }

    private void appendField(List<String> fields, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        fields.add(label + ": " + normalize(value));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("\\s+", " ").trim();
    }
}
