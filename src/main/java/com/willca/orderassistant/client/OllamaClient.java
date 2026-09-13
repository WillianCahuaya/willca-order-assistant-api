package com.willca.orderassistant.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Component
public class OllamaClient {

    private final WebClient webClient;

    public OllamaClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:11434").build();
    }

    public Mono<String> chat(String message) {
        String systemPrompt = "You are an assistant for an order management system.";
        Map<String, Object> request = Map.of(
                "model", "llama3.2",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", message
                        )
                ),
                "stream", false
        );

        return webClient.post()
                .uri("/api/chat")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response ->
                        response.at("/message/content").asText()
                );
    }

}
