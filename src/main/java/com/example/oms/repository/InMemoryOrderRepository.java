package com.example.oms.repository;

import com.example.oms.model.Order;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;
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
    public Page<Order> findAll(Pageable pageable) {
        List<Order> allOrders = store.values().stream()
                .map(this::copyOf)
                .sorted(createSorter(pageable.getSort()))
                .collect(Collectors.toList());
        
        return paginateList(allOrders, pageable);
    }

    @Override
    public void delete(Order order) {
        store.remove(order.getId());
    }

    @Override
    public Page<Order> findByStatus(OrderStatus status, Pageable pageable) {
        List<Order> filteredOrders = store.values().stream()
                .filter(o -> o.getStatus() == status)
                .map(this::copyOf)
                .sorted(createSorter(pageable.getSort()))
                .collect(Collectors.toList());
                
        return paginateList(filteredOrders, pageable);
    }
    
    @Deprecated
    @Override
    public List<Order> findAll() {
        return store.values().stream()
                .map(this::copyOf)
                .collect(Collectors.toList());
    }

    @Deprecated
    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return store.values().stream()
                .filter(o -> o.getStatus() == status)
                .map(this::copyOf)
                .collect(Collectors.toList());
    }
    
    private Page<Order> paginateList(List<Order> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        
        if (start > list.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, list.size());
        }
        
        return new PageImpl<>(
            list.subList(start, end), 
            pageable, 
            list.size()
        );
    }
    
    private Comparator<Order> createSorter(Sort sort) {
        if (sort.isUnsorted()) {
            return (o1, o2) -> 0;
        }
        
        return sort.stream()
            .map(order -> {
                Comparator<Order> comparator;
                switch (order.getProperty().toLowerCase()) {
                    case "id":
                        comparator = Comparator.comparing(Order::getId);
                        break;
                    case "customerid":
                        comparator = Comparator.comparing(Order::getCustomerId);
                        break;
                    case "createdat":
                        comparator = Comparator.comparing(Order::getCreatedAt);
                        break;
                    case "status":
                        comparator = Comparator.comparing(Order::getStatus);
                        break;
                    case "totalamount":
                        comparator = Comparator.comparing(Order::getTotalAmount);
                        break;
                    default:
                        comparator = Comparator.comparing(Order::getCreatedAt);
                }
                return order.isAscending() ? comparator : comparator.reversed();
            })
            .reduce(Comparator::thenComparing)
            .orElse((o1, o2) -> 0);
    }

    private Order copyOf(Order order) {
        List<OrderItem> itemsCopy = new ArrayList<>(order.getItems());
        return new Order(
            order.getId(), 
            order.getCustomerId(), 
            order.getCustomerName(),
            order.getShippingAddress(),
            order.getCreatedBy(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getStatus(),
            order.getTotalAmount(),
            itemsCopy
        );
    }
}


