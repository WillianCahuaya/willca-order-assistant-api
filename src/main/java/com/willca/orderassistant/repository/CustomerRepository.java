package com.willca.orderassistant.repository;

import com.willca.orderassistant.model.Customer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CustomerRepository
        extends ReactiveMongoRepository<Customer, String> {
}
