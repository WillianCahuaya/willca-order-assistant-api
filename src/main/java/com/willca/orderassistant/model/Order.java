package com.willca.orderassistant.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document("orders")
public record Order(
        @Id
        String id,
        String customerId,
        String status,
        BigDecimal total,
        String restaurantId
) {
}