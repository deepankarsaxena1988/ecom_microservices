package com.ecom.pgvector.search;

import java.util.List;

public interface EmbeddingModelClient {
    List<Double> embed(String text);
}
