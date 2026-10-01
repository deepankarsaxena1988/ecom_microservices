package com.ecom.pgvector.search;

/**
 * Request payload for PUT /api/embedding/config. Only the profile key is
 * required (e.g. "openai" or "qwen") - all connection details are resolved
 * server-side from application.yml (app.embedding.profiles.*).
 */
public class EmbeddingConfigRequest {

    private String profile;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}

