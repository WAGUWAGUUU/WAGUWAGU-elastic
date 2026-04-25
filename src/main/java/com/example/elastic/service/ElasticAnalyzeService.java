package com.example.elastic.service;

import com.example.elastic.domain.entity.ElasticStores;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticAnalyzeService {

    private final WebClient.Builder webClientBuilder;
    @Value("${spring.elasticsearch.uris:${spring.elasticsearch.rest.uris:http://localhost:9200}}")
    private String elasticsearchUris;

    public Mono<List<Map<String, Object>>> analyzeEntity(ElasticStores entity) {
        String combinedText = combineEntityFields(entity);

        return webClientBuilder.build()
                .post()
                .uri(resolveAnalyzeUri())
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
                    log.error("Error during Elasticsearch analysis", e);
                    return Mono.just(Collections.emptyList());
                });
    }

    private String resolveAnalyzeUri() {
        String baseUri = elasticsearchUris.split(",")[0].trim();
        return baseUri.replaceAll("/+$", "") + "/_analyze";
    }

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
