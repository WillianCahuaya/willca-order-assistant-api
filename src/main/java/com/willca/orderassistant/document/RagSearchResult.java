package com.willca.orderassistant.document;

public record RagSearchResult(
        String orderId,
        String status,
        Double distance,
        String content
) {
}
