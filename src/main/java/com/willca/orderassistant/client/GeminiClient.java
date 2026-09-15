package com.willca.orderassistant.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;

    private final ObjectMapper objectMapper;

    public GeminiClient(WebClient.Builder builder,
                        ObjectMapper objectMapper,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.base-url}") String baseUrl,
            @Value("${gemini.model}") String model) {

        this.webClient = builder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    private URI geminiUri(UriBuilder uriBuilder) {
        return uriBuilder
                .path("/v1beta/models/" + model + ":generateContent")
                .queryParam("key", apiKey)
                .build();
    }

    public Mono<String> chatWithContext(String systemPrompt, String context, String message) {

        log.info("GEMINI_CLIENT_CONTEXT INIT:\nsystemPrompt: [{}]\ncontext: [{}]\nmessage: [{}]", systemPrompt, context, message);

        Map<String, Object> request = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(
                                Map.of("text", systemPrompt)
                        )
                ),
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", "Context:\n" + context),
                                        Map.of("text", "User:\n" + message)
                                )
                        )
                )
        );

        //log.info("GEMINI_CLIENT_CONTEXT REQUEST:\n[{}]", request);

        return webClient.post()
                .uri(this::geminiUri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchangeToMono(this::handleGeminiResponse)
                .map(response -> response.at("/candidates/0/content/parts/0/text").asText())
                .doOnSuccess(result -> log.info("GEMINI_CLIENT_CONTEXT RESPONSE:\n[{}]", result));
    }

    private Mono<JsonNode> handleGeminiResponse(ClientResponse response) {
        if (response.statusCode().isError()) {
            return response.bodyToMono(String.class)
                    .flatMap(body -> {
                        log.error("Gemini error >>> {}", body);
                        return Mono.error(new RuntimeException("Gemini error: " + body));
                    });
        }
        return response.bodyToMono(JsonNode.class);
    }


    public Mono<String> chatWithTools(String message) {

        Map<String, Object> findOrdersByStatus = Map.of(
                "name", "findOrdersByStatus",
                "description", "Find orders by their current status",
                "parameters", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                                "status", Map.of(
                                        "type", "STRING",
                                        "description", """
                                            Order status such as PENDING, CONFIRMED,
                                            PREPARING, SHIPPED, DELIVERED or CANCELLED
                                            """
                                )
                        ),
                        "required", List.of("status")
                )
        );

        Map<String, Object> findOrderById = Map.of(
                "name", "findOrderById",
                "description", "Find a specific order by its ID",
                "parameters", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                                "orderId", Map.of(
                                        "type", "STRING",
                                        "description", "The unique ID of the order"
                                )
                        ),
                        "required", List.of("orderId")
                )
        );

        Map<String, Object> findCustomerById = Map.of(
                "name", "findCustomerById",
                "description", "Find a specific customer by its ID",
                "parameters", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                                "customerId", Map.of(
                                        "type", "STRING",
                                        "description", "The unique ID of the customer"
                                )
                        ),
                        "required", List.of("customerId")
                )
        );

        Map<String, Object> tool = Map.of(
                "functionDeclarations", List.of(
                        findOrdersByStatus,
                        findOrderById,
                        findCustomerById)
        );

        Map<String, Object> request = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", message)
                                )
                        )
                ),
                "tools", List.of(tool)
        );

        //log.info("GEMINI TOOL CLIENT REQUEST >>> {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));

        return webClient.post()
                .uri(this::geminiUri)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleGeminiError)
                .bodyToMono(String.class)
                .doOnSuccess(resp -> log.info("GEMINI TOOL CLIENT SUCCESS"));
    }

    public Mono<String> chatWithToolResponse(List<Map<String, Object>> historyContents) {

        Map<String, Object> requestContents = Map.of("contents", historyContents);
        //printContents(requestContents);

        return webClient.post()
                .uri(this::geminiUri)
                .bodyValue(requestContents)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleGeminiError)
                .bodyToMono(String.class);
                //.doOnNext(response -> log.info("GEMINI TOOL RESPONSE >>> {}", response));
    }


    private void printContents(Map<String, Object> requestContents) {
        log.info("TOOL RESPONSE REQUEST >>>");
        try {
            String requestLog = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(requestContents)
                    .replaceAll("\"thoughtSignature\"\\s*:\\s*\"[^\"]*\"", "\"thoughtSignature\": \"...\"");
            log.info(requestLog);
        } catch (Exception e) {
            log.error("Error printing contents", e);
        }
    }

    private Mono<? extends Throwable> handleGeminiError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("GEMINI ERROR: {}", body);
                    return Mono.error(new RuntimeException("Gemini API error: " + body));
                });
    }

}