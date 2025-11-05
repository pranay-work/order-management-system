package com.example.oms.repository;

import com.example.oms.model.Order;
import com.example.oms.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    Page<Order> findAll(Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    // Keep the old methods for backward compatibility
    @Deprecated
    default List<Order> findAll() {
        return findAll(Pageable.unpaged()).getContent();
    }
    
    @Deprecated
    default List<Order> findByStatus(OrderStatus status) {
        return findByStatus(status, Pageable.unpaged()).getContent();
    }
}


