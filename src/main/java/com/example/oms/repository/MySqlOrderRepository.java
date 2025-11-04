package com.example.oms.repository;

import com.example.oms.model.Order;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;
import com.example.oms.repository.jpa.OrderEntity;
import com.example.oms.repository.jpa.OrderItemEntity;
import com.example.oms.repository.jpa.JpaOrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "repository.type",
        havingValue = "mysql",
        matchIfMissing = true
)
@org.springframework.context.annotation.Primary
public class MySqlOrderRepository implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;

    public MySqlOrderRepository(JpaOrderRepository jpaOrderRepository) {
        this.jpaOrderRepository = jpaOrderRepository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = toEntity(order);
        OrderEntity saved = jpaOrderRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaOrderRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return jpaOrderRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return jpaOrderRepository.findByStatus(status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity(
                order.getId(),
                order.getCustomerId(),
                order.getCreatedAt(),
                order.getStatus()
        );

        List<OrderItemEntity> itemEntities = order.getItems().stream()
                .map(item -> {
                    OrderItemEntity itemEntity = new OrderItemEntity(item.getProductId(), item.getQuantity());
                    itemEntity.setOrder(entity);
                    return itemEntity;
                })
                .collect(Collectors.toList());

        entity.setItems(itemEntities);
        return entity;
    }

    private Order toDomain(OrderEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(item -> new OrderItem(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());

        Order order = new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getCreatedAt(),
                entity.getStatus(),
                items
        );
        return order;
    }
}

