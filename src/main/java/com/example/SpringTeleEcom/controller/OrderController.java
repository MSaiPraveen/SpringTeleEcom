package com.example.SpringTeleEcom.controller;

import com.example.SpringTeleEcom.model.dto.OrderRequest;
import com.example.SpringTeleEcom.model.dto.OrderResponse;
import com.example.SpringTeleEcom.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 🔹 Place order – authenticated user
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse orderResponse = orderService.placeOrder(orderRequest);
        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    // 🔹 Get orders for current logged-in user (MyOrders.jsx)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/orders/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        List<OrderResponse> orderResponseList = orderService.getCurrentUserOrderResponses();
        return new ResponseEntity<>(orderResponseList, HttpStatus.OK);
    }

    // 🔹 Get all orders – ADMIN only (Order.jsx)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orderResponseList = orderService.getAllOrderResponses();
        return new ResponseEntity<>(orderResponseList, HttpStatus.OK);
    }



    // 🔹 Update order status – ADMIN only
    // Frontend: PUT /api/orders/{orderId}/status?status=SHIPPED
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam String status
    ) {
        boolean updated = orderService.updateStatus(orderId, status);
        if (updated) {
            return ResponseEntity.ok("Order status updated to: " + status);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Order not found");
    }
}
