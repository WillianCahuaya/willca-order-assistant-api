package com.willca.orderassistant.iaservice;

import com.willca.orderassistant.document.OrderDocument;
import com.willca.orderassistant.rag.OrderCsvReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class RagIngestionService {

    private final OrderCsvReader csvReader;
    private final EmbeddingService embeddingService;
    private final DatabaseClient databaseClient;

    public RagIngestionService(OrderCsvReader csvReader,
                               EmbeddingService embeddingService,
                               DatabaseClient databaseClient) {
        this.csvReader = csvReader;
        this.embeddingService = embeddingService;
        this.databaseClient = databaseClient;
    }

    public Mono<Void> ingest() throws IOException {

        List<OrderDocument> documents = csvReader.read();
        log.info("RAG_INGESTION_SERVICE INIT >>> Starting | documents={}", documents.size());

        return Flux.fromIterable(documents)
                .flatMap(document ->
                    embeddingService.embed(document)
                            .flatMap(embedding -> save(document, embedding))
                )
                .then()
                .doOnSuccess(ignored -> log.info("RAG_INGESTION_SERVICE RESPONSE >>> Finished | documentsSize={}", documents.size()));
    }

    private Mono<Void> save(OrderDocument document, List<Double> embedding) {

        log.info("RAG_INGESTION_SERVICE_SAVE INIT");

        String content = """
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

        String vector = embedding.toString()
                .replace("[", "[")
                .replace("]", "]");

        return databaseClient.sql("""
                INSERT INTO order_embeddings (
                    order_id,
                    customer_id,
                    status,
                    total,
                    restaurant_id,
                    content,
                    embedding
                )
                VALUES (
                    :orderId,
                    :customerId,
                    :status,
                    :total,
                    :restaurantId,
                    :content,
                    CAST(:embedding AS vector)
                )
                ON CONFLICT (order_id)
                DO UPDATE SET
                    customer_id = EXCLUDED.customer_id,
                    status = EXCLUDED.status,
                    total = EXCLUDED.total,
                    restaurant_id = EXCLUDED.restaurant_id,
                    content = EXCLUDED.content,
                    embedding = EXCLUDED.embedding
                """)
                .bind("orderId", document.orderId())
                .bind("customerId", document.customerId())
                .bind("status", document.status())
                .bind("total", document.total())
                .bind("restaurantId", document.restaurantId())
                .bind("content", content)
                .bind("embedding", vector)
                .fetch()
                .rowsUpdated()
                .doOnNext(rows -> log.debug("RAG_INGESTION_SERVICE_SAVE RESPONSE >>> Saved | orderId={} | rows={}", document.orderId(), rows ))
                .doOnError(error -> log.error("RAG_INGESTION_SERVICE_SAVE ERROR >>> Failed | error={}", error.getMessage(), error))
                .then();
    }
}
