package com.willca.orderassistant.dto;

public record AssistantRequest(
        String orderId,
        String status,
        String message
) {
}
