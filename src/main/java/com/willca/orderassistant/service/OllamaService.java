package com.willca.orderassistant.service;

import com.willca.orderassistant.client.OllamaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class OllamaService {

    private final OllamaClient ollamaClient;

    public Mono<String> ask(String message) {
        return ollamaClient.chat(message)
                .doOnSuccess(result -> log.info("Probando con OLLAMA\nRequest >>>> [{}]\nResponse >>> [{}]", message, result));
    }
}