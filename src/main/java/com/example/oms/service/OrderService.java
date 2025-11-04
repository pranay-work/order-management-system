package com.example.oms.service;

import com.example.oms.model.Order;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;
import com.example.oms.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(String customerName, List<OrderItem> items) {
        Order order = new Order(UUID.randomUUID(), customerName, Instant.now(), OrderStatus.PENDING, items);
        return orderRepository.save(order);
    }

    public Order getOrder(UUID id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<Order> listOrders(Optional<OrderStatus> status) {
        return status.map(orderRepository::findByStatus).orElseGet(orderRepository::findAll);
    }

    public Order updateOrderStatus(UUID id, OrderStatus newStatus) {
        Order existing = getOrder(id);
        if (existing.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderOperationException("Cannot update a cancelled order.");
        }
        existing.setStatus(newStatus);
        return orderRepository.save(existing);
    }

    public Order cancelOrder(UUID id) {
        Order existing = getOrder(id);
        if (existing.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderOperationException("Only PENDING orders can be cancelled.");
        }
        existing.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(existing);
    }

    public int processPendingOrders() {
        List<Order> pending = orderRepository.findByStatus(OrderStatus.PENDING);
        int updated = 0;
        for (Order order : pending) {
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
            updated++;
        }
        return updated;
    }

    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(UUID id) {
            super("Order not found: " + id);
        }
    }

    public static class InvalidOrderOperationException extends RuntimeException {
        public InvalidOrderOperationException(String message) {
            super(message);
        }
    }
}


