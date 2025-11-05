package com.example.oms.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Order {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private String shippingAddress;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItem> items;

    public Order() {
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.totalAmount = BigDecimal.ZERO;
    }

    // Constructor for simplified in-memory repository
    public Order(UUID id, UUID customerId, Instant createdAt, OrderStatus status, List<OrderItem> items) {
        this.id = id;
        this.customerId = customerId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = this.createdAt;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.totalAmount = calculateTotalAmount(this.items);
        
        // Set order reference in items
        this.items.forEach(item -> item.setOrder(this));
    }
    
    public Order(UUID id, UUID customerId, String customerName, String shippingAddress, 
                String createdBy, Instant createdAt, Instant updatedAt, 
                OrderStatus status, BigDecimal totalAmount, List<OrderItem> items) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.shippingAddress = shippingAddress;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
        this.status = status != null ? status : OrderStatus.PENDING;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        
        // Set order reference in items
        if (this.items != null) {
            this.items.forEach(item -> item.setOrder(this));
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<OrderItem> getItems() {
        return items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        // Update order reference in items
        if (this.items != null) {
            this.items.forEach(item -> item.setOrder(this));
        }
    }

    public void removeItem(OrderItem item) {
        if (items != null) {
            items.remove(item);
            if (item != null) {
                item.setOrder(null);
            }
        }
    }

    public void addItem(OrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        if (item != null) {
            items.add(item);
            item.setOrder(this);
        }
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
            .map(OrderItem::getTotalPrice)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
