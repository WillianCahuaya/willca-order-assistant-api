package com.willca.orderassistant.service;

import com.willca.orderassistant.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderContextService {

    private final OrderService orderService;

    public Mono<String> getContextById(String orderId) {
        return orderService.findById(orderId)
                .map(this::formatOrder);
    }

    public Mono<String> getContextByStatus(String status) {
        return orderService.findByStatus(status)
                .map(this::formatOrder)
                .collectList()
                .map(orders -> String.join("\n", orders));
    }

    private String formatOrder(Order order) {

        return """
                ID: %s
                Customer: %s
                Status: %s
                Total: $%s
                Restaurant: %s
                """.formatted(
                order.id(),
                order.customerId(),
                order.status(),
                order.total(),
                order.restaurantId()
        );
    }

}
