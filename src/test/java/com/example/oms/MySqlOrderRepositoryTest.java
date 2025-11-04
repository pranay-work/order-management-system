package com.example.oms;

import com.example.oms.model.Order;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;
import com.example.oms.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "repository.type=mysql",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class MySqlOrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        // Clean up before each test (if needed)
    }

    @Test
    void saveAndFindOrder() {
        UUID customerId = UUID.randomUUID();
        Order order = new Order(
                UUID.randomUUID(),
                customerId,
                java.time.Instant.now(),
                OrderStatus.PENDING,
                List.of(new OrderItem("P1", 2), new OrderItem("P2", 1))
        );

        Order saved = orderRepository.save(order);
        assertNotNull(saved);
        assertEquals(order.getId(), saved.getId());

        Optional<Order> found = orderRepository.findById(order.getId());
        assertTrue(found.isPresent());
        Order retrieved = found.get();
        assertEquals(order.getId(), retrieved.getId());
        assertEquals(customerId, retrieved.getCustomerId());
        assertEquals(OrderStatus.PENDING, retrieved.getStatus());
        assertEquals(2, retrieved.getItems().size());
    }

    @Test
    void updateOrderStatus() {
        UUID customerId = UUID.randomUUID();
        Order order = new Order(
                UUID.randomUUID(),
                customerId,
                java.time.Instant.now(),
                OrderStatus.PENDING,
                List.of(new OrderItem("P3", 5))
        );

        Order saved = orderRepository.save(order);
        saved.setStatus(OrderStatus.PROCESSING);
        Order updated = orderRepository.save(saved);

        Optional<Order> found = orderRepository.findById(order.getId());
        assertTrue(found.isPresent());
        assertEquals(OrderStatus.PROCESSING, found.get().getStatus());
    }

    @Test
    void findAllOrders() {
        UUID customerId1 = UUID.randomUUID();
        UUID customerId2 = UUID.randomUUID();

        orderRepository.save(new Order(
                UUID.randomUUID(), customerId1, java.time.Instant.now(),
                OrderStatus.PENDING, List.of(new OrderItem("P1", 1))
        ));
        orderRepository.save(new Order(
                UUID.randomUUID(), customerId2, java.time.Instant.now(),
                OrderStatus.PROCESSING, List.of(new OrderItem("P2", 2))
        ));

        List<Order> all = orderRepository.findAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void findByStatus() {
        UUID customerId = UUID.randomUUID();
        Order pendingOrder = new Order(
                UUID.randomUUID(),
                customerId,
                java.time.Instant.now(),
                OrderStatus.PENDING,
                List.of(new OrderItem("P1", 1))
        );
        orderRepository.save(pendingOrder);

        List<Order> pending = orderRepository.findByStatus(OrderStatus.PENDING);
        assertTrue(pending.size() >= 1);
        assertTrue(pending.stream().anyMatch(o -> o.getId().equals(pendingOrder.getId())));
    }

    @Test
    void findByIdNotFound() {
        Optional<Order> found = orderRepository.findById(UUID.randomUUID());
        assertFalse(found.isPresent());
    }
}

