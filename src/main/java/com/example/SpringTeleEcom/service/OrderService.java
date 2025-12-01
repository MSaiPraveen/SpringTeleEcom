package com.example.SpringTeleEcom.service;

import com.example.SpringTeleEcom.model.Order;
import com.example.SpringTeleEcom.model.OrderItem;
import com.example.SpringTeleEcom.model.Product;
import com.example.SpringTeleEcom.model.User;
import com.example.SpringTeleEcom.model.dto.OrderItemRequest;
import com.example.SpringTeleEcom.model.dto.OrderItemResponse;
import com.example.SpringTeleEcom.model.dto.OrderRequest;
import com.example.SpringTeleEcom.model.dto.OrderResponse;
import com.example.SpringTeleEcom.repo.OrderRepo;
import com.example.SpringTeleEcom.repo.ProductRepo;
import com.example.SpringTeleEcom.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;
    private final UserRepository userRepository;

    public OrderService(ProductRepo productRepo,
                        OrderRepo orderRepo,
                        UserRepository userRepository) {
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.userRepository = userRepository;
    }

    // 🔹 Place order for the currently logged-in user
    public OrderResponse placeOrder(OrderRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Order order = new Order();
        String orderId = "ORD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        order.setOrderId(orderId);
        order.setCustomerName(request.customerName());
        order.setEmail(request.email());
        order.setStatus("PLACED");
        order.setOrderDate(LocalDate.now());
        order.setUser(user);

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemReq : request.items()) {

            Product product = productRepo.findById(Math.toIntExact(itemReq.productId()))
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.productId()));

            // reduce stock
            int newStock = product.getStockQuantity() - itemReq.quantity();
            if (newStock < 0) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            product.setStockQuantity(newStock);
            productRepo.save(product);

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.quantity()));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .totalPrice(lineTotal)
                    .order(order)
                    .build();

            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        Order savedOrder = orderRepo.save(order);

        return mapToOrderResponse(savedOrder);
    }

    // 🔹 Orders for current logged-in user (MyOrders.jsx)
    public List<OrderResponse> getCurrentUserOrderResponses() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<Order> orders = orderRepo.findByUserId(user.getId());

        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    // 🔹 All orders (Admin)
    public List<OrderResponse> getAllOrderResponses() {
        List<Order> orders = orderRepo.findAll();
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    // 🔹 Update order status (Admin)
    public boolean updateStatus(String orderId, String newStatus) {
        Order order = orderRepo.findByOrderId(orderId);
        if (order == null) {
            return false;
        }
        order.setStatus(newStatus.toUpperCase());
        orderRepo.save(order);
        return true;
    }

    // 🔹 Helper: Order → OrderResponse DTO
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getOrderId(),
                order.getCustomerName(),
                order.getEmail(),
                order.getStatus(),
                order.getOrderDate(),
                itemResponses
        );
    }
}
