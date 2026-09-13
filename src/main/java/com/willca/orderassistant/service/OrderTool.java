package com.willca.orderassistant.service;

import com.willca.orderassistant.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderTool {

    private final OrderService orderService;

    public Flux<Order> findOrdersByStatus(String status) {
        return orderService.findByStatus(status);
    }

    public Mono<Order> findOrderById(String orderId) {
        return orderService.findById(orderId);
    }

}
