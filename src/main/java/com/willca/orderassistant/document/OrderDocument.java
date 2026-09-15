package com.willca.orderassistant.document;

import java.math.BigDecimal;

public record OrderDocument(
        String orderId,
        String customerId,
        String status,
        BigDecimal total,
        String restaurantId
) {
}
