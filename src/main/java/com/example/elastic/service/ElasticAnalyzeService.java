package com.example.elastic.service;

import com.example.elastic.domain.entity.ElasticStores;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ElasticAnalyzeService {

    private final WebClient.Builder webClientBuilder;
    private RestClient restClient;

    // Method to analyze the entity using Elasticsearch's _analyze API
    public Mono<List<Map<String, Object>>> analyzeEntity(ElasticStores entity) {
        String combinedText = combineEntityFields(entity);

        return webClientBuilder.build()
                .post()
                .uri("http://localhost:9200/_analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "tokenizer", "nori_tokenizer",
                        "text", combinedText
                ))
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("Elasticsearch error: " + error)))
                )
                .bodyToMono(Map.class)
                .flatMap(body -> Mono.justOrEmpty((List<Map<String, Object>>) body.get("tokens")))
                .onErrorResume(e -> {
                    // Log the error and return an empty list or a default response
                    System.err.println("Error during Elasticsearch analysis: " + e.getMessage());
                    return Mono.just(Collections.emptyList());
                });
    }

    // Close the RestClient when done
    public void close() throws IOException {
        this.restClient.close();
    }

    // Helper method to combine fields of the entity into a single string for analysis
    private String combineEntityFields(ElasticStores entity) {
        return String.join(" ",
                entity.getStoreName() != null ? entity.getStoreName() : "",
                entity.getStoreIntroduction() != null ? entity.getStoreIntroduction() : "",
                entity.getMenuName() != null ? entity.getMenuName() : "",
                entity.getMenuIntroduction() != null ? entity.getMenuIntroduction() : "",
                entity.getCustomerId() != null ? entity.getCustomerId().toString() : ""
        ).trim();
    }
}
