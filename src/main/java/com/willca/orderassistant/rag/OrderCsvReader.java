package com.willca.orderassistant.rag;

import com.willca.orderassistant.document.OrderDocument;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderCsvReader {

    public List<OrderDocument> read() throws IOException {

        ClassPathResource resource = new ClassPathResource("rag/documents/orders.csv");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return reader.lines()
                    .skip(1)
                    .map(this::toOrderDocument)
                    .toList();
        }
    }

    private OrderDocument toOrderDocument(String line) {

        String[] columns = line.split(",");
        return new OrderDocument(
                columns[0],
                columns[1],
                columns[2],
                new BigDecimal(columns[3]),
                columns[4]
        );
    }
}
