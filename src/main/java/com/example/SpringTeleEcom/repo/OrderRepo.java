package com.example.SpringTeleEcom.repo;

import com.example.SpringTeleEcom.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Long> {

    // All orders for a given user
    List<Order> findByUserId(Long userId);

    // Find by public-facing orderId like "ORDXXXX"
    Order findByOrderId(String orderId);
}
