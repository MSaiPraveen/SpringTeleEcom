package com.example.SpringTeleEcom.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String orderId;

    private String customerName;
    private String email;

    private String status;
    private LocalDate orderDate;

    // Order totals
    private BigDecimal subtotal;    // Sum of all items
    private BigDecimal tax;         // Tax amount (e.g., 10% of subtotal)
    private BigDecimal totalAmount; // Subtotal + Tax

    // 🔥 ADD THIS — link order → user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Order items - using "items" field name to match your getOrderItems() method
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items;

    // Lombok will generate getItems() and setItems()
    // But you also have getOrderItems() method, so adding alias methods
    public List<OrderItem> getOrderItems() {
        return items;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.items = orderItems;
    }
}
