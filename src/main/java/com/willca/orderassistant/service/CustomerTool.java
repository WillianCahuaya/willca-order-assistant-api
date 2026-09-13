package com.willca.orderassistant.service;

import com.willca.orderassistant.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomerTool {

    private final CustomerService customerService;

    public Mono<Customer> findCustomerById(String customerId) {
        return customerService.findById(customerId);
    }
}
