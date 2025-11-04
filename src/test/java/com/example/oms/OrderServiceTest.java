package com.example.oms;

import com.example.oms.model.Order;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;
import com.example.oms.repository.InMemoryOrderRepository;
import com.example.oms.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {

    private OrderService orderService;

    @BeforeEach
    void setup() {
        orderService = new OrderService(new InMemoryOrderRepository());
    }

    @Test
    void createAndGetOrder() {
        Order created = orderService.createOrder("Alice", List.of(new OrderItem("P1", 2)));
        Order fetched = orderService.getOrder(created.getId());
        assertEquals(created.getId(), fetched.getId());
        assertEquals(OrderStatus.PENDING, fetched.getStatus());
    }

    @Test
    void cancelOnlyPending() {
        Order created = orderService.createOrder("Bob", List.of(new OrderItem("P2", 1)));
        // OK: cancel pending
        Order cancelled = orderService.cancelOrder(created.getId());
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());

        // Fail: cannot cancel non-pending
        Order created2 = orderService.createOrder("Carl", List.of(new OrderItem("P3", 1)));
        orderService.updateOrderStatus(created2.getId(), OrderStatus.PROCESSING);
        assertThrows(OrderService.InvalidOrderOperationException.class,
                () -> orderService.cancelOrder(created2.getId()));
    }

    @Test
    void schedulerMovesPendingToProcessing() {
        Order created = orderService.createOrder("Dana", List.of(new OrderItem("P4", 3)));
        int updated = orderService.processPendingOrders();
        assertEquals(1, updated);
        Order fetched = orderService.getOrder(created.getId());
        assertEquals(OrderStatus.PROCESSING, fetched.getStatus());
    }

    @Test
    void updateStatus() {
        Order created = orderService.createOrder("Eve", List.of(new OrderItem("P5", 5)));
        Order updated = orderService.updateOrderStatus(created.getId(), OrderStatus.SHIPPED);
        assertEquals(OrderStatus.SHIPPED, updated.getStatus());
    }

    @Test
    void notFound() {
        UUID randomId = UUID.randomUUID();
        assertThrows(OrderService.OrderNotFoundException.class, () -> orderService.getOrder(randomId));
    }
}


