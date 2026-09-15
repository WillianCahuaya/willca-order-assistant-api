package com.willca.orderassistant.tool;

import com.willca.orderassistant.model.Customer;
import com.willca.orderassistant.service.CustomerService;
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
