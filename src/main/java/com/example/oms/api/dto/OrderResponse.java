package com.example.oms.api.dto;

import com.example.oms.model.Order;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OrderResponse {
    private UUID id;
    private String customerName;
    private Instant createdAt;
    private OrderStatus status;
    private List<OrderItem> items;

    public static OrderResponse from(Order order) {
        OrderResponse r = new OrderResponse();
        r.id = order.getId();
        r.customerName = order.getCustomerName();
        r.createdAt = order.getCreatedAt();
        r.status = order.getStatus();
        r.items = order.getItems();
        return r;
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}


