package com.willca.orderassistant.controller;

import com.willca.orderassistant.dto.AssistantRequest;
import com.willca.orderassistant.service.GeminiContextService;
import com.willca.orderassistant.service.GeminiToolService;
import com.willca.orderassistant.service.OllamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/assistant")
public class AssistantController {

    private final GeminiContextService geminiContextService;
    private final GeminiToolService geminiToolService;
    private final OllamaService ollamaService;

    @PostMapping("/gemini/id")
    public Mono<String> geminiById(@RequestBody AssistantRequest request) {
        return geminiContextService.askById(request);
    }

    @PostMapping("/gemini/status")
    public Mono<String> geminiByStatus(@RequestBody AssistantRequest request) {
        return geminiContextService.askByStatus(request);
    }

    @PostMapping("/gemini/tool")
    public Mono<String> geminiTool(@RequestBody String request) {
        return geminiToolService.askWithTool(request);
    }

    @PostMapping("/ollama")
    public Mono<String> ollama(@RequestBody AssistantRequest request) {
        return ollamaService.ask(request.message());
    }
}
