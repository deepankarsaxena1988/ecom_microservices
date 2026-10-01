package com.ecom.pgvector.search;

/**
 * Response payload describing the currently active embedding profile.
 * The API key is intentionally never echoed back (only whether one is configured).
 */
public class EmbeddingConfigResponse {

    private final String profile;
    private final String provider;
    private final String model;
    private final String baseUrl;
    private final boolean apiKeyConfigured;
    private final Iterable<String> availableProfiles;

    public EmbeddingConfigResponse(String profile, String provider, String model, String baseUrl,
                                    boolean apiKeyConfigured, Iterable<String> availableProfiles) {
        this.profile = profile;
        this.provider = provider;
        this.model = model;
        this.baseUrl = baseUrl;
        this.apiKeyConfigured = apiKeyConfigured;
        this.availableProfiles = availableProfiles;
    }

    public String getProfile() {
        return profile;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isApiKeyConfigured() {
        return apiKeyConfigured;
    }

    public Iterable<String> getAvailableProfiles() {
        return availableProfiles;
    }
}

