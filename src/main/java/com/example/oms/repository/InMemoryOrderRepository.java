package com.example.oms.repository;

import com.example.oms.model.Order;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@org.springframework.stereotype.Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "repository.type",
        havingValue = "memory"
)
public class InMemoryOrderRepository implements OrderRepository {

    private final ConcurrentMap<UUID, Order> store = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        // Create a defensive copy to store
        // todo 
        Order copy = copyOf(order);
        store.put(copy.getId(), copy);
        return copyOf(copy);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        Order order = store.get(id);
        return Optional.ofNullable(order).map(this::copyOf);
    }

    @Override
    public List<Order> findAll() {
        return store.values().stream().map(this::copyOf).collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return store.values().stream()
                .filter(o -> o.getStatus() == status)
                .map(this::copyOf)
                .collect(Collectors.toList());
    }

    private Order copyOf(Order order) {
        List<OrderItem> itemsCopy = new ArrayList<>(order.getItems());
        return new Order(order.getId(), order.getCustomerId(), order.getCreatedAt(), order.getStatus(), itemsCopy);
    }
}


