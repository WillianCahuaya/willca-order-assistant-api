package com.willca.orderassistant.iaservice;

import com.willca.orderassistant.client.GeminiEmbeddingClient;
import com.willca.orderassistant.document.RagSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class RagSearchService {

    private final GeminiEmbeddingClient geminiEmbeddingClient;
    private final DatabaseClient databaseClient;

    public Mono<List<RagSearchResult>> search(String question, int limit) {

        log.info("RAG_SEARCH_SERVICE INIT");

        return geminiEmbeddingClient.embed(question)
                .flatMap(response -> {
                    String vector = response.embedding().values().toString();
                    log.info("RAG_SEARCH_SERVICE >>> Executing similarity search");

                    return databaseClient.sql("""
                            SELECT order_id, status, content, embedding <=> CAST(:embedding AS vector) AS distance
                            FROM order_embeddings
                            ORDER BY distance
                            LIMIT :limit
                            """)
                            .bind("embedding", vector)
                            .bind("limit", limit)
                            .map((row, metadata) -> {
                                String orderId = row.get("order_id", String.class);
                                String status = row.get("status", String.class);
                                String content = row.get("content", String.class);
                                Double distance = row.get("distance", Double.class);
                                log.info("RAG_SEARCH_SERVICE >>> Match | orderId={} | status={} | distance={}", orderId, status, distance);
                                return new RagSearchResult(orderId, status, distance, content);
                            })
                            .all()
                            .collectList();
                })
                .doOnSuccess(results -> log.info("RAG_SEARCH_SERVICE >>> Completed | resultsSize={}", results == null ? 0 : results.size()))
                .doOnError(error -> log.error("RAG SEARCH >>> Failed | error={}", error.getMessage(), error));
    }
}

