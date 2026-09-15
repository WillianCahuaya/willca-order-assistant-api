package com.willca.orderassistant.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
public class GeminiEmbeddingClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final int dimensions;

    public GeminiEmbeddingClient(
            WebClient.Builder builder,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.base-url}") String baseUrl,
            @Value("${gemini.embedding.model}") String model,
            @Value("${gemini.embedding.dimensions}") int dimensions) {

        this.webClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.model = model;
        this.dimensions = dimensions;
    }

    private URI geminiEmbeddingUri(UriBuilder uriBuilder) {
        return uriBuilder
                .path("/v1beta/models/" + model + ":embedContent")
                .queryParam("key", apiKey)
                .build();
    }

    public Mono<EmbeddingResponse> embed(String text) {

        log.info("GEMINI_EMBEDDING_CLIENT INIT: text: [{}]", text);

        EmbeddingRequest request = new EmbeddingRequest(new Content(List.of(new Part(text))), dimensions);

        return webClient.post()
                .uri(this::geminiEmbeddingUri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .doOnSuccess(result -> log.info("GEMINI_EMBEDDING_CLIENT RESPONSE >>> Query embedding generated | embeddingSize: [{}]",
                        result.embedding.values.size()));
    }

    public record EmbeddingRequest(Content content, Integer output_dimensionality) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    public record EmbeddingResponse(Embedding embedding) {}

    public record Embedding(List<Double> values) {}
}