package com.example.oms.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Order {
    private UUID id;
    private String customerName;
    private Instant createdAt;
    private OrderStatus status;
    private List<OrderItem> items;

    public Order() {
    }

    public Order(UUID id, String customerName, Instant createdAt, OrderStatus status, List<OrderItem> items) {
        this.id = id;
        this.customerName = customerName;
        this.createdAt = createdAt;
        this.status = status;
        this.items = new ArrayList<>(items);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<OrderItem> items) {
        this.items = new ArrayList<>(items);
    }
}


