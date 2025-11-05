package com.example.oms.repository;

import com.example.oms.model.Order;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;
import com.example.oms.repository.jpa.OrderEntity;
import com.example.oms.repository.jpa.OrderItemEntity;
import com.example.oms.repository.jpa.JpaOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional(readOnly = true)
public class MySqlOrderRepository implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;

    public MySqlOrderRepository(JpaOrderRepository jpaOrderRepository) {
        this.jpaOrderRepository = jpaOrderRepository;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity = toEntity(order);
        OrderEntity saved = jpaOrderRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void delete(Order order) {
        if (order != null && order.getId() != null) {
            jpaOrderRepository.deleteById(order.getId());
        }
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaOrderRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return jpaOrderRepository.findAllWithItems(pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Order> findByStatus(OrderStatus status, Pageable pageable) {
        return jpaOrderRepository.findByStatusWithItems(status, pageable)
                .map(this::toDomain);
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
        if (entity == null) {
            return null;
        }
        
        List<OrderItem> items = entity.getItems() != null ? 
            entity.getItems().stream()
                .map(item -> {
                    OrderItem orderItem = new OrderItem();
                    // Convert Long itemId to UUID if needed, or use null if itemId is null
                    if (item.getItemId() != null) {
                        orderItem.setId(UUID.nameUUIDFromBytes(item.getItemId().toString().getBytes()));
                    }
                    orderItem.setProductId(item.getProductId());
                    orderItem.setProductName(item.getProductName() != null ? item.getProductName() : "");
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setUnitPrice(item.getUnitPrice());
                    return orderItem;
                })
                .collect(Collectors.toList()) : 
            List.of();

        return new Order(
            entity.getId(),
            entity.getCustomerId(),
            entity.getCreatedAt(),
            entity.getStatus(),
            items
        );
    }
}

