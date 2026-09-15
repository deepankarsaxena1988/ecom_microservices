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

@Service
@ConditionalOnProperty(prefix = "app.embedding", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenAiEmbeddingClient implements EmbeddingModelClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiEmbeddingClient.class);

    private final EmbeddingModelProperties properties;
    private final RestTemplate restTemplate;

    public OpenAiEmbeddingClient(EmbeddingModelProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<Double> embed(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        if (!StringUtils.hasText(properties.getApiKey())) {
            log.warn("No OpenAI API key configured. Falling back to deterministic embedding for local execution.");
            return fallbackEmbedding(text);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        Map<String, Object> requestBody = Map.of(
                "input", text,
                "model", properties.getModel()
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        String url = properties.getBaseUrl().replaceAll("/+$", "") + "/embeddings";

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
