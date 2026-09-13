package com.willca.orderassistant.service;

import com.willca.orderassistant.client.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiToolService {

    private final GeminiClient geminiClient;
    private final OrderTool orderTool;
    private final CustomerTool customerTool;
    private final ObjectMapper objectMapper;

    public Mono<String> askWithTool(String message) {

        log.info("GEMINI INIT SERVICE MESSAGE >>> {}", message);
        List<Map<String, Object>> historyContents = new ArrayList<>();
        historyContents.add(
                Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", message))
                )
        );
        return geminiClient.chatWithTools(message)
                .flatMap(response -> processGeminiResponse(response, message, historyContents));
    }

    private Mono<String> processGeminiResponse(String response, String message,
                                               List<Map<String, Object>> historyContents) {
        log.info("PROCESS GEMINI RESPONSE ...");
        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode content = root
                    .path("candidates").get(0)
                    .path("content");

            historyContents.add(objectMapper.convertValue(content, Map.class));

            JsonNode parts = content.path("parts");

            List<JsonNode> functionCalls = new ArrayList<>();

            for (JsonNode part : parts) {
                JsonNode functionCall = part.path("functionCall");
                if (!functionCall.isMissingNode()) {
                    functionCalls.add(functionCall);
                }
            }
            log.info("Function calls detected >>> {}", functionCalls.size());

            for (JsonNode functionCall : functionCalls) {
                log.info("Gemini requested tool >>> name={}, id={}, args={}",
                        functionCall.path("name").asText(),
                        functionCall.path("id").asText(),
                        functionCall.path("args")
                );
            }

            if (!functionCalls.isEmpty()) {
                return processToolCalls(functionCalls, message, historyContents);
            }

            String finalResponseText = "";
            for (JsonNode part : parts) {
                JsonNode text = part.path("text");
                if (!text.isMissingNode() && !text.asText().isBlank()) {
                    finalResponseText = text.asText();
                }
            }
            log.info("GEMINI FINAL RESPONSE >>> {}", finalResponseText);

            return Mono.just(finalResponseText);

        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private Mono<String> processToolCalls(List<JsonNode> functionCalls, String message,
                                          List<Map<String, Object>> historyContents) {

        return Flux.fromIterable(functionCalls)
                .flatMap(functionCall -> executeTool(functionCall))
                .collectList()
                .flatMap(functionResponses -> {
                    historyContents.addAll(functionResponses);
                    log.info( "Sending {} tool response(s) back to Gemini", functionResponses.size());
                    return geminiClient.chatWithToolResponse(historyContents);
                })
                .flatMap(response -> processGeminiResponse(response, message, historyContents));
    }

    private Mono<Map<String, Object>> executeTool(JsonNode functionCall) {

        String functionName = functionCall.path("name").asText();
        String functionCallId = functionCall.path("id").asText();
        JsonNode functionArgs = functionCall.path("args");
        log.info("Tool execution START >>> name={}, id={}, args={}", functionName, functionCallId, functionArgs);

        Mono<Map<String, Object>> toolResponse = switch (functionName) {

            case "findOrdersByStatus" -> {
                String status = functionArgs.path("status").asText();
                yield orderTool.findOrdersByStatus(status)
                        .collectList()
                        .map(orders -> buildFunctionResponse(functionName, functionCallId, orders));
            }

            case "findOrderById" -> {
                String orderId = functionArgs.path("orderId").asText();
                yield orderTool.findOrderById(orderId)
                        .map(order -> buildFunctionResponse(functionName, functionCallId, order));
            }

            case "findCustomerById" -> {
                String customerId = functionArgs.path("customerId").asText();
                yield customerTool.findCustomerById(customerId)
                        .map(customer -> buildFunctionResponse(functionName, functionCallId, customer));
            }

            default -> Mono.error(new IllegalArgumentException("Unknown function: " + functionName));
        };
        return toolResponse
                .doOnNext(result ->
                        log.info("Tool result >>> name={}, id={}, result={}", functionName, functionCallId, result)
                );
    }

    private Map<String, Object> buildFunctionResponse(String functionName, String functionCallId, Object result) {
        return Map.of(
                "role", "user",
                "parts", List.of(
                        Map.of(
                                "functionResponse",
                                Map.of(
                                        "name", functionName,
                                        "id", functionCallId,
                                        "response", Map.of(
                                                "result", result
                                        )
                                )
                        )
                )
        );
    }
}
