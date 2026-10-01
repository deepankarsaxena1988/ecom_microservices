package com.ecom.pgvector.search;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Binds named embedding model profiles from application.yml (app.embedding.profiles.*)
 * plus which profile is active by default at startup. A UI only needs to send the
 * profile key (e.g. "openai" or "qwen") to /api/embedding/config; the backend resolves
 * provider/model/baseUrl/apiKey from this configuration.
 *
 * Example application.yml:
 * app:
 *   embedding:
 *     enabled: true
 *     active-profile: openai
 *     profiles:
 *       openai:
 *         provider: openai
 *         model: text-embedding-3-small
 *         base-url: https://api.openai.com/v1
 *         api-key: ${OPENAI_API_KEY:}
 *         dimension: 1536
 *       qwen:
 *         provider: ollama
 *         model: qwen3-embedding:0.6b
 *         base-url: http://localhost:11434
 *         api-key:
 *         dimension: 1024
 *
 * The `dimension` of each profile must match one of the fixed-size vector
 * columns on product_vector (see EmbeddingVectorColumnResolver) so pgvector
 * can index and compare vectors of that model without dimension mismatches.
 */
@ConfigurationProperties(prefix = "app.embedding")
public class EmbeddingModelProperties {

    private boolean enabled = true;
    private String activeProfile = "openai";
    private Map<String, EmbeddingProfile> profiles = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
    }

    public Map<String, EmbeddingProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<String, EmbeddingProfile> profiles) {
        this.profiles = profiles;
    }

    public static class EmbeddingProfile {
        private String provider;
        private String model;
        private String baseUrl;
        private String apiKey;
        private int dimension;

        public int getDimension() {
            return dimension;
        }

        public void setDimension(int dimension) {
            this.dimension = dimension;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
