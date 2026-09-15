package com.willca.orderassistant.iaservice;

import com.willca.orderassistant.client.GeminiClient;
import com.willca.orderassistant.document.RagSearchResult;
import com.willca.orderassistant.dto.AssistantRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiContextService {

    private final GeminiClient geminiClient;
    private final OrderContextService orderContextService;
    private final RagSearchService ragSearchService;

    public Mono<String> askById(AssistantRequest request) {

        log.info("GEMINI_SERVICE_CONTEXT_BY_ID INIT:\nrequest: [{}]", request);

        String systemPrompt = """
            You are an assistant for an order management system.

            Answer clearly and concisely using the provided context.
            """;
        return orderContextService.getContextById(request.orderId())
                .flatMap(context -> geminiClient.chatWithContext(systemPrompt, context, request.message()));
    }

    public Mono<String> askByStatus(AssistantRequest request) {

        log.info("GEMINI_SERVICE_CONTEXT_BY_STATUS INIT:\nrequest: [{}]", request);

        String systemPrompt = """
            You are an assistant for an order management system.

            Answer clearly and concisely using the provided context.
            """;
        return orderContextService.getContextByStatus(request.status())
                .flatMap(context -> geminiClient.chatWithContext(systemPrompt, context, request.message()));
    }

    public Mono<String> askWithRag(String question, int limit) {

        log.info("GEMINI_SERVICE_CONTEXT_WITH_RAG INIT: question: [{}], limit: [{}]", question, limit);

        return ragSearchService.search(question, limit)
                .map(this::buildContext)
                .flatMap(context -> {
                    String systemPrompt = """
                            You are an assistant for an order management system.

                            Answer the user's question using only the information
                            provided in the context.

                            If the answer cannot be found in the context,
                            say that the information is not available.
                            """;
                    return geminiClient.chatWithContext(systemPrompt, context, question);
                });
    }

    private String buildContext(List<RagSearchResult> results) {
        return results.stream()
                .map(RagSearchResult::content)
                .collect(Collectors.joining("\n\n---\n\n"));
    }

}
