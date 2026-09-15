package com.willca.orderassistant.iaservice;

import com.willca.orderassistant.client.GeminiEmbeddingClient;
import com.willca.orderassistant.document.OrderDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final GeminiEmbeddingClient embeddingClient;

    public Mono<List<Double>> embed(String text) {
        return embeddingClient.embed(text)
                .map(response -> response.embedding().values());
    }

    public Mono<List<Double>> embed(OrderDocument document) {

        log.info("RAG_EMBEDDING_SERVICE INIT >>> Processing | orderDocument={}", document);

        String text = """
                Order %s
                Customer: %s
                Status: %s
                Total: %s
                Restaurant: %s
                """.formatted(
                document.orderId(),
                document.customerId(),
                document.status(),
                document.total(),
                document.restaurantId()
        );

        return embeddingClient.embed(text)
                .map(response -> response.embedding().values())
                .doOnNext(embedding ->
                  log.info("RAG_EMBEDDING_SERVICE RESPONSE >>> Generated | orderId={} | dimensions={}", document.orderId(), embedding.size()));
    }
}
