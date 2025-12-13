package com.example.SpringTeleEcom.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderResponse(
        String orderId,
        String customerName,
        String email,
        String status,
        LocalDate orderDate,
        List<OrderItemResponse> items,
        BigDecimal subtotal,    // Sum of all items before tax
        BigDecimal tax,         // Tax amount
        BigDecimal totalAmount  // Subtotal + Tax
) {
}
