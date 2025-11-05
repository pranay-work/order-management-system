package com.example.oms.service;

import com.example.oms.model.Order;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;
import com.example.oms.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(UUID customerId, List<OrderItem> items) {
        Order order = new Order(UUID.randomUUID(), customerId, Instant.now(), OrderStatus.PENDING, items);
        return orderRepository.save(order);
    }

    public Order getOrder(UUID id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    /**
     * List orders with pagination
     * @param status Optional status filter
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @param sortBy Field to sort by (default: createdAt)
     * @param direction Sort direction (ASC/DESC, default: DESC)
     * @return Page of orders
     */
    public Page<Order> listOrders(Optional<OrderStatus> status, int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = sortBy == null || sortBy.trim().isEmpty() ? "createdAt" : sortBy;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        
        return status.map(s -> orderRepository.findByStatus(s, pageable))
                    .orElseGet(() -> orderRepository.findAll(pageable));
    }
    
    /**
     * Backward compatible method to get all orders without pagination
     * @deprecated Use listOrders with pagination parameters instead
     */
    @Deprecated
    public List<Order> listOrders(Optional<OrderStatus> status) {
        return status.map(s -> orderRepository.findByStatus(s, Pageable.unpaged()).getContent())
                   .orElseGet(() -> orderRepository.findAll(Pageable.unpaged()).getContent());
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


