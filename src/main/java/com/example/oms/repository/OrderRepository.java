package com.example.oms.repository;

import com.example.oms.model.Order;
import com.example.oms.model.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    List<Order> findAll();
    List<Order> findByStatus(OrderStatus status);
}


