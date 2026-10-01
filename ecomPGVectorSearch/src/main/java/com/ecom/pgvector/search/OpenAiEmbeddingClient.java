package com.ecom.pgvector.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates embeddings by routing to the currently active provider/model held in
 * {@link EmbeddingRuntimeConfig}. Supports:
 * - "openai": calls {baseUrl}/embeddings with {input, model} and bearer auth.
 * - "ollama": calls {baseUrl}/api/embeddings with {model, prompt} (used for local
 *   models such as Qwen embedding models, e.g. "qwen3-embedding:0.6b").
 * Falls back to a deterministic local embedding only when no OpenAI API key is
 * configured, so the app remains usable without external credentials.
 */
@Service
@ConditionalOnProperty(prefix = "app.embedding", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenAiEmbeddingClient implements EmbeddingModelClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiEmbeddingClient.class);

    private final EmbeddingRuntimeConfig runtimeConfig;
    private final RestTemplate restTemplate;

    public OpenAiEmbeddingClient(EmbeddingRuntimeConfig runtimeConfig) {
        this.runtimeConfig = runtimeConfig;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<Double> embed(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        String provider = runtimeConfig.getProvider();
        if (EmbeddingRuntimeConfig.PROVIDER_OLLAMA.equalsIgnoreCase(provider)) {
            return embedWithOllama(text);
        }
        return embedWithOpenAi(text);
    }

    private List<Double> embedWithOpenAi(String text) {
        if (!StringUtils.hasText(runtimeConfig.getApiKey())) {
            log.warn("No OpenAI API key configured. Falling back to deterministic embedding for local execution.");
            return fallbackEmbedding(text);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(runtimeConfig.getApiKey());

        Map<String, Object> requestBody = Map.of(
                "input", text,
                "model", runtimeConfig.getModel()
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        String url = runtimeConfig.getBaseUrl().replaceAll("/+$", "") + "/embeddings";

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getBody() == null || response.getBody().get("data") == null) {
            throw new IllegalStateException("Embedding generation response was empty");
        }

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
        if (data == null || data.isEmpty()) {
            throw new IllegalStateException("Embedding generation returned no data");
        }

        List<Object> values = (List<Object>) data.get(0).get("embedding");
        if (values == null || values.isEmpty()) {
            throw new IllegalStateException("Embedding vector was empty");
        }

        return toDoubleList(values);
    }

    private List<Double> embedWithOllama(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "model", runtimeConfig.getModel(),
                "prompt", text
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        String url = runtimeConfig.getBaseUrl().replaceAll("/+$", "") + "/api/embeddings";

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getBody() == null || response.getBody().get("embedding") == null) {
            throw new IllegalStateException("Ollama embedding response was empty");
        }

        List<Object> values = (List<Object>) response.getBody().get("embedding");
        if (values.isEmpty()) {
            throw new IllegalStateException("Ollama embedding vector was empty");
        }

        return toDoubleList(values);
    }

    private List<Double> toDoubleList(List<Object> values) {
        List<Double> vector = new ArrayList<>(values.size());
        for (Object value : values) {
            vector.add(((Number) value).doubleValue());
        }
        return vector;
    }

    private List<Double> fallbackEmbedding(String text) {
        double seed = 0.0D;
        for (int i = 0; i < text.length(); i++) {
            seed += text.charAt(i) * (i + 1) * 0.01D;
        }

        List<Double> vector = new ArrayList<>(1536);
        for (int i = 0; i < 1536; i++) {
            double value = Math.sin(seed + i * 0.73D) * 0.9D;
            vector.add(value);
        }
        return vector;
    }
}
