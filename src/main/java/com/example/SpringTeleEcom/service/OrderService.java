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
import java.math.RoundingMode;
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

        System.out.println("🛒 PlaceOrder - Starting order creation");
        System.out.println("   Customer: " + request.customerName());
        System.out.println("   Email: " + request.email());
        System.out.println("   Items count: " + request.items().size());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        System.out.println("   Authenticated user: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        System.out.println("   User ID: " + user.getId());

        Order order = new Order();
        String orderId = "ORD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        order.setOrderId(orderId);
        order.setCustomerName(request.customerName());
        order.setEmail(request.email());
        order.setStatus("PLACED");
        order.setOrderDate(LocalDate.now());
        order.setUser(user);

        System.out.println("   Generated Order ID: " + orderId);

        List<OrderItem> orderItems = new ArrayList<>();

        System.out.println("📝 Processing " + request.items().size() + " item(s) from request:");

        int itemIndex = 0;
        for (OrderItemRequest itemReq : request.items()) {
            itemIndex++;

            System.out.println("   Item " + itemIndex + ":");
            System.out.println("      Product ID: " + itemReq.productId());
            System.out.println("      Quantity Requested: " + itemReq.quantity());

            Product product = productRepo.findById(Math.toIntExact(itemReq.productId()))
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.productId()));

            System.out.println("      Product Name: " + product.getName());
            System.out.println("      Product Price: $" + product.getPrice());

            // reduce stock
            int newStock = product.getStockQuantity() - itemReq.quantity();
            if (newStock < 0) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            product.setStockQuantity(newStock);
            productRepo.save(product);

            System.out.println("      Stock before: " + (newStock + itemReq.quantity()));
            System.out.println("      Stock after: " + newStock);

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.quantity()));

            System.out.println("      Line Total: $" + lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .totalPrice(lineTotal)
                    .order(order)
                    .build();

            System.out.println("      ✅ OrderItem created with quantity: " + orderItem.getQuantity());

            orderItems.add(orderItem);
            System.out.println("      ✅ Added to orderItems list (current size: " + orderItems.size() + ")");
        }

        System.out.println("📦 Total OrderItems created: " + orderItems.size());

        order.setOrderItems(orderItems);

        // Calculate totals
        BigDecimal subtotal = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate tax (10%)
        BigDecimal taxRate = new BigDecimal("0.10");
        BigDecimal tax = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);

        // Calculate total
        BigDecimal totalAmount = subtotal.add(tax);

        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setTotalAmount(totalAmount);

        System.out.println("💰 Order Totals:");
        System.out.println("   Subtotal: $" + subtotal);
        System.out.println("   Tax (10%): $" + tax);
        System.out.println("   Total: $" + totalAmount);

        Order savedOrder = orderRepo.save(order);

        System.out.println("✅ Order saved successfully:");
        System.out.println("   Order ID: " + savedOrder.getOrderId());
        System.out.println("   Database ID: " + savedOrder.getId());
        System.out.println("   Items: " + savedOrder.getOrderItems().size());
        System.out.println("   Total Amount: $" + savedOrder.getTotalAmount());

        System.out.println("   📋 Saved Items Details:");
        for (int i = 0; i < savedOrder.getOrderItems().size(); i++) {
            OrderItem item = savedOrder.getOrderItems().get(i);
            System.out.println("      Item " + (i + 1) + ": " + item.getProduct().getName() +
                             " x " + item.getQuantity() + " = $" + item.getTotalPrice());
        }

        return mapToOrderResponse(savedOrder);
    }

    // 🔹 Orders for current logged-in user (MyOrders.jsx)
    public List<OrderResponse> getCurrentUserOrderResponses() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            System.err.println("❌ No authentication found in SecurityContext");
            throw new RuntimeException("User not authenticated");
        }

        String username = auth.getName();
        System.out.println("📦 Getting orders for user: " + username);
        System.out.println("   Authorities: " + auth.getAuthorities());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.err.println("❌ User not found in database: " + username);
                    System.err.println("   This OAuth user may not have been saved properly during login");
                    return new RuntimeException("User not found: " + username);
                });

        System.out.println("✅ User found: " + user.getUsername() + " (ID: " + user.getId() + ")");

        List<Order> orders = orderRepo.findByUserId(user.getId());

        System.out.println("📋 Found " + orders.size() + " orders for user");

        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    // 🔹 All orders (Admin)
    public List<OrderResponse> getAllOrderResponses() {
        System.out.println("🔍 OrderService - Getting all orders from database");

        List<Order> orders = orderRepo.findAll();

        System.out.println("📊 Total orders in database: " + orders.size());

        if (!orders.isEmpty()) {
            System.out.println("📋 Order details:");
            orders.forEach(order -> {
                System.out.println("   - Order ID: " + order.getOrderId() +
                                 ", Customer: " + order.getCustomerName() +
                                 ", Status: " + order.getStatus() +
                                 ", Items: " + order.getOrderItems().size());
            });
        } else {
            System.out.println("⚠️ No orders found in database! Check if orders are being saved properly.");
        }

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
                itemResponses,
                order.getSubtotal(),
                order.getTax(),
                order.getTotalAmount()
        );
    }
}
