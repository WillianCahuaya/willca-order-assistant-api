package com.willca.orderassistant.controller;

import com.willca.orderassistant.model.Customer;
import com.willca.orderassistant.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public Mono<Customer> create(@RequestBody Customer customer) {
        return customerService.create(customer);
    }

    @PostMapping("/batch")
    public Flux<Customer> createBatch(@RequestBody List<Customer> customers) {
        return customerService.createBatch(customers);
    }

    @GetMapping
    public Flux<Customer> findAll() {
        return customerService.findAll();
    }

    @GetMapping("/{customerId}")
    public Mono<Customer> findById(@PathVariable String customerId) {
        return customerService.findById(customerId);
    }

    @PutMapping("/{customerId}")
    public Mono<Customer> update(@PathVariable String customerId, @RequestBody Customer customer) {
        Customer updated = new Customer(customerId, customer.name());
        return customerService.update(updated);
    }

    @DeleteMapping("/{customerId}")
    public Mono<Void> deleteById(@PathVariable String customerId) {
        return customerService.deleteById(customerId);
    }
}