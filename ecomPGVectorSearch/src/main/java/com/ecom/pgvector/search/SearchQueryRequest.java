package com.ecom.pgvector.search;

public record SearchQueryRequest(String query, Integer limit) {
}
