package com.willca.orderassistant.service;

import com.willca.orderassistant.model.Order;
import com.willca.orderassistant.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;

    public Mono<Order> create(Order order) {
        return repository.save(order);
    }

    public Flux<Order> createBatch(List<Order> orders) {
        return repository.saveAll(orders);
    }

    public Flux<Order> findAll() { return repository.findAll(); }

    public Mono<Order> findById(String orderId) { return repository.findById(orderId);}

    public Flux<Order> findByStatus(String status) { return repository.findByStatus(status);}
}
