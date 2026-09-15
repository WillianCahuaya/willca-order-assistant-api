package com.willca.orderassistant.controller;

import com.willca.orderassistant.document.RagSearchResult;
import com.willca.orderassistant.iaservice.EmbeddingService;
import com.willca.orderassistant.iaservice.GeminiContextService;
import com.willca.orderassistant.iaservice.RagIngestionService;
import com.willca.orderassistant.iaservice.RagSearchService;
import com.willca.orderassistant.rag.OrderCsvReader;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final EmbeddingService embeddingService;
    private final RagIngestionService ingestionService;
    private final RagSearchService searchService;
    private final GeminiContextService geminiContextService;
    private final OrderCsvReader csvReader;

    @GetMapping("/embedding")
    public Mono<List<Double>> embedding(@RequestParam String text) {
        return embeddingService.embed(text);
    }

    @GetMapping("/embed-csv")
    public Mono<List<List<Double>>> embedCsv() throws IOException {
        return Flux.fromIterable(csvReader.read())
                .flatMap(embeddingService::embed)
                .collectList();
    }

    @PostMapping("/ingest")
    public Mono<Void> ingest() throws IOException {
        return ingestionService.ingest();
    }

    @GetMapping("/search")
    public Mono<List<RagSearchResult>> search(@RequestParam String query, @RequestParam(defaultValue = "3") int limit) {
        return searchService.search(query, limit);
    }

    @GetMapping("/ask-with-rag")
    public Mono<String> askWithRag(@RequestParam String question, @RequestParam(defaultValue = "3") int limit) {
        return geminiContextService.askWithRag(question, limit);
    }
}
