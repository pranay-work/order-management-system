package com.example.oms.service;

import com.example.oms.exception.ResourceNotFoundException;
import com.example.oms.model.Order;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;
import com.example.oms.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Creates a new order
     * @param order The order to create
     * @return The created order with generated ID and timestamps
     */
    /**
     * Creates a new order
     * @param order The order to create
     * @return The created order with generated ID and timestamps
     */
    @Transactional
    public Order createOrder(Order order) {
        // Set initial status and timestamps
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        
        // Set order reference in items
        if (order.getItems() != null) {
            order.getItems().forEach(item -> item.setOrder(order));
        }
        
        // Calculate and set order total
        calculateOrderTotal(order);
        
        // Save the order
        return orderRepository.save(order);
    }
    
    /**
     * Creates a new order with the given customer ID and items
     * @param customerId The customer ID
     * @param items The order items
     * @return The created order
     */
    @Transactional
    public Order createOrder(UUID customerId, List<OrderItem> items) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setItems(items);
        return createOrder(order);
    }

    /**
     * Retrieves an order by ID
     * @param id The order ID
     * @return The order if found
     * @throws ResourceNotFoundException if the order is not found
     */
    public Optional<Order> getOrder(UUID id) {
        return orderRepository.findById(id);
    }

    /**
     * Lists all orders with pagination and optional status filter
     * @param pageable Pagination information
     * @return Page of orders
     */
    /**
     * Lists all orders with pagination
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @param sortBy Field to sort by
     * @param direction Sort direction (ASC/DESC)
     * @return Page of orders
     */
    public Page<Order> listOrders(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = sortBy == null || sortBy.trim().isEmpty() ? "createdAt" : sortBy;
        Pageable pageable = PageRequest.of(page, size, sortDirection, sortField);
        return orderRepository.findAll(pageable);
    }
    
    /**
     * Finds orders by status with pagination
     * @param status The order status to filter by
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @param sortBy Field to sort by
     * @param direction Sort direction (ASC/DESC)
     * @return Page of orders with the specified status
     */
    public Page<Order> findByStatus(OrderStatus status, int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = sortBy == null || sortBy.trim().isEmpty() ? "createdAt" : sortBy;
        Pageable pageable = PageRequest.of(page, size, sortDirection, sortField);
        return orderRepository.findByStatus(status, pageable);
    }
    
    /**
     * Backward compatible method to get all orders without pagination
     * @deprecated Use listOrders with pagination parameters instead
     */
    /**
     * Backward compatible method to get all orders without pagination
     * @deprecated Use listOrders with pagination parameters instead
     */
    @Deprecated
    public List<Order> listOrders(OrderStatus status) {
        return status != null 
            ? orderRepository.findByStatus(status, Pageable.unpaged()).getContent()
            : orderRepository.findAll(Pageable.unpaged()).getContent();
    }

    /**
     * Updates the status of an order
     * @param id The order ID
     * @param newStatus The new status
     * @return The updated order if found, empty otherwise
     */
    @Transactional
    public Optional<Order> updateOrderStatus(UUID id, OrderStatus newStatus) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(newStatus);
            order.setUpdatedAt(Instant.now());
            return orderRepository.save(order);
        });
    }

    /**
     * Cancels an order if it's not already shipped or delivered
     * @param id The order ID
     * @return The cancelled order if found and not already in a final state, empty otherwise
     */
    @Transactional
    public Optional<Order> cancelOrder(UUID id) {
        return orderRepository.findById(id).map(order -> {
            if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
                throw new IllegalStateException("Cannot cancel an order that has already been " + 
                    order.getStatus().name().toLowerCase());
            }
            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(Instant.now());
            return orderRepository.save(order);
        });
    }
    
    /**
     * Deletes an order by ID
     * @param id The order ID
     * @return true if the order was found and deleted, false otherwise
     */
    @Transactional
    public boolean deleteOrder(UUID id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * Calculates and sets the total price of an order based on its items
     * @param order The order to calculate the total for
     */
    private void calculateOrderTotal(Order order) {
        if (order.getItems() != null) {
            BigDecimal total = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalAmount(total);
        } else {
            order.setTotalAmount(BigDecimal.ZERO);
        }
    }

    /**
     * Processes all pending orders by changing their status to PROCESSING
     * @return The number of processed orders
     */
    @Transactional
    public int processPendingOrders() {
        List<Order> pending = orderRepository.findByStatus(OrderStatus.PENDING, Pageable.unpaged()).getContent();
        int updated = 0;
        for (Order order : pending) {
            order.setStatus(OrderStatus.PROCESSING);
            order.setUpdatedAt(Instant.now());
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


