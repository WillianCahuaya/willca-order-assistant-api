package com.willca.orderassistant.service;

import com.willca.orderassistant.model.Customer;
import com.willca.orderassistant.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;

    public Mono<Customer> create(Customer customer) {
        return repository.save(customer);
    }

    public Flux<Customer> createBatch(List<Customer> customers) {
        return repository.saveAll(customers);
    }

    public Flux<Customer> findAll() {
        return repository.findAll();
    }

    public Mono<Customer> findById(String customerId) {
        return repository.findById(customerId);
    }

    public Mono<Customer> update(Customer customer) {
        return repository.save(customer);
    }

    public Mono<Void> deleteById(String customerId) {
        return repository.deleteById(customerId);
    }
}
