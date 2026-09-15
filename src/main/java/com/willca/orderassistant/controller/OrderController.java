package com.willca.orderassistant.controller;

import com.willca.orderassistant.model.Order;
import com.willca.orderassistant.iaservice.OrderContextService;
import com.willca.orderassistant.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;
    private final OrderContextService orderContextService;

    @PostMapping
    public Mono<Order> create(@RequestBody Order order) {
        return service.create(order);
    }

    @PostMapping("/batch")
    public Flux<Order> createBatch(@RequestBody List<Order> orders) {
        return service.createBatch(orders);
    }

    @GetMapping
    public Flux<Order> findAll() { return service.findAll();}

    @GetMapping("/status/{status}")
    public Flux<Order> ordersByStatus(@PathVariable String status) {
        return service.findByStatus(status);
    }
}
