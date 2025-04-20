package com.example.neura_search.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {
    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.embedding.model}")
    private String embeddingModel;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    private final RestTemplate restTemplate;

    public EmbeddingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public float[] generateEmbedding(String text) {
        // Limit text length to stay within token limits
        String truncatedText = text.length() > 8000 ? text.substring(0, 8000) : text;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", truncatedText);
        requestBody.put("model", embeddingModel);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(
                    openaiApiUrl + "/embeddings",
                    request,
                    Map.class);

            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                List<Double> embedding = (List<Double>) data.get(0).get("embedding");

                float[] result = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    result[i] = embedding.get(i).floatValue();
                }

                return result;
            }
        } catch (Exception e) {
            // Fall back to mock embedding if OpenAI API fails
            return generateMockEmbedding();
        }

        return generateMockEmbedding();
    }

    private float[] generateMockEmbedding() {
        // Generate mock embedding for testing/fallback
        float[] mockEmbedding = new float[1536]; // OpenAI's ada-002 dimension
        for (int i = 0; i < mockEmbedding.length; i++) {
            mockEmbedding[i] = (float) Math.random() * 2 - 1; // Random values between -1 and 1
        }

        // Normalize the vector
        float sum = 0;
        for (float v : mockEmbedding) {
            sum += v * v;
        }
        float norm = (float) Math.sqrt(sum);
        for (int i = 0; i < mockEmbedding.length; i++) {
            mockEmbedding[i] = mockEmbedding[i] / norm;
        }

        return mockEmbedding;
    }
}
