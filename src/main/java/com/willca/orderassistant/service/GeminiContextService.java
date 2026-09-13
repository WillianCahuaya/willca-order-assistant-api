package com.willca.orderassistant.service;

import com.willca.orderassistant.client.GeminiClient;
import com.willca.orderassistant.dto.AssistantRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiContextService {

    private final GeminiClient geminiClient;
    private final OrderContextService orderContextService;

    public Mono<String> askById(AssistantRequest request) {
        String systemPrompt = """
            You are an assistant for an order management system.

            Answer clearly and concisely using the provided context.
            """;
        return orderContextService.getContextById(request.orderId())
                .flatMap(context -> geminiClient.chatWithContext(systemPrompt, context, request.message())
                ).doOnSuccess(result ->
                        log.info("GEMINI CONTEXT RESPONSE\nRequest >>>> [{}]\nResponse >>> [{}]", request, result));
    }

    public Mono<String> askByStatus(AssistantRequest request) {
        String systemPrompt = """
            You are an assistant for an order management system.

            Answer clearly and concisely using the provided context.
            """;
        return orderContextService.getContextByStatus(request.status())
                .flatMap(context -> geminiClient.chatWithContext(systemPrompt, context, request.message())
                ).doOnSuccess(result ->
                        log.info("GEMINI CONTEXT RESPONSE\nRequest >>>> [{}]\nResponse >>> [{}]", request, result));
    }

}
