package com.ecom.pgvector.search;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Holds the currently active embedding profile key and resolves its
 * provider/model/baseUrl/apiKey from the named profiles configured in
 * application.yml (app.embedding.profiles.*). A UI only needs to send the
 * profile key (e.g. "openai" or "qwen") via /api/embedding/config to switch
 * providers at runtime without restarting the app.
 */
@Component
public class EmbeddingRuntimeConfig {

    public static final String PROVIDER_OPENAI = "openai";
    public static final String PROVIDER_OLLAMA = "ollama";

    private final Map<String, EmbeddingModelProperties.EmbeddingProfile> profiles;
    private volatile String activeProfileKey;

    public EmbeddingRuntimeConfig(EmbeddingModelProperties properties) {
        this.profiles = properties.getProfiles();
        if (profiles == null || profiles.isEmpty()) {
            throw new IllegalStateException(
                    "No embedding profiles configured under app.embedding.profiles in application.yml");
        }
        String defaultProfile = properties.getActiveProfile();
        if (!profiles.containsKey(defaultProfile)) {
            throw new IllegalStateException(
                    "app.embedding.active-profile '" + defaultProfile + "' is not defined under app.embedding.profiles");
        }
        this.activeProfileKey = defaultProfile;
    }

    public boolean hasProfile(String profileKey) {
        return profileKey != null && profiles.containsKey(profileKey);
    }

    public Iterable<String> getAvailableProfiles() {
        return profiles.keySet();
    }

    public String getActiveProfileKey() {
        return activeProfileKey;
    }

    public String getProvider() {
        return currentProfile().getProvider();
    }

    public String getModel() {
        return currentProfile().getModel();
    }

    public String getBaseUrl() {
        return currentProfile().getBaseUrl();
    }

    public String getApiKey() {
        return currentProfile().getApiKey();
    }

    public int getDimension() {
        return currentProfile().getDimension();
    }

    /**
     * Switches the active profile by key only (e.g. "qwen"). All connection
     * details are read from the matching application.yml profile, not the caller.
     */
    public synchronized void switchProfile(String profileKey) {
        if (!hasProfile(profileKey)) {
            throw new IllegalArgumentException(
                    "Unknown embedding profile '" + profileKey + "'. Available: " + profiles.keySet());
        }
        this.activeProfileKey = profileKey;
    }

    private EmbeddingModelProperties.EmbeddingProfile currentProfile() {
        return profiles.get(activeProfileKey);
    }
}

