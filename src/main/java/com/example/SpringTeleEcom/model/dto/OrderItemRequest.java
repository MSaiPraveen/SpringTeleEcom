package com.example.SpringTeleEcom.model.dto;

public record OrderItemRequest(
    long productId,
    int quantity
){}
