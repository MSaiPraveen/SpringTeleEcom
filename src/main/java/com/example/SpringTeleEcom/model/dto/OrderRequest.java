package com.example.SpringTeleEcom.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(
        String customerName,
        String email,
        List<OrderItemRequest> items,
        BigDecimal subtotal,    // Frontend-calculated subtotal
        BigDecimal shipping,    // Frontend-calculated shipping cost
        BigDecimal tax,         // Frontend-calculated tax (10% of subtotal)
        BigDecimal totalAmount  // Frontend-calculated total (subtotal + shipping + tax)
) {
}
