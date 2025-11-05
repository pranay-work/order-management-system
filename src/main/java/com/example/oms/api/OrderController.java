package com.example.oms.api;

import com.example.oms.api.dto.CreateOrderRequest;
import com.example.oms.api.dto.OrderItemRequest;
import com.example.oms.api.dto.OrderResponse;
import com.example.oms.api.dto.UpdateOrderStatusRequest;
import com.example.oms.api.dto.PaginatedResponse;
import com.example.oms.model.Order;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;
import com.example.oms.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = toDomain(request);
        Order createdOrder = orderService.createOrder(order);
        OrderResponse response = OrderResponse.from(createdOrder);
        return ResponseEntity.created(URI.create("/api/orders/" + createdOrder.getId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return orderService.getOrder(id)
                .map(OrderResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<OrderResponse>> listOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Page<Order> orderPage = orderService.listOrders(
            status,
            page, 
            size, 
            sortBy, 
            direction
        );
        
        List<OrderResponse> content = orderPage.getContent().stream()
            .map(OrderResponse::from)
            .toList();
            
        PaginatedResponse<OrderResponse> response = new PaginatedResponse<>(
            content,
            orderPage.getNumber(),
            orderPage.getSize(),
            orderPage.getTotalElements(),
            orderPage.getTotalPages(),
            orderPage.isFirst(),
            orderPage.isLast()
        );
        
        return ResponseEntity.ok(response);
    }

    @RequestMapping(path = "/{id}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable("id") UUID id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(id, request.getStatus())
                .map(OrderResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable("id") UUID id) {
        return orderService.cancelOrder(id)
                .map(OrderResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private Order toDomain(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerId(UUID.fromString(request.getCustomerId()));
        
        List<OrderItem> items = request.getItems().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
            
        order.setItems(items);
        return order;
    }
    
    private OrderItem toDomain(OrderItemRequest itemRequest) {
        return new OrderItem(
            itemRequest.getProductId(),
            itemRequest.getProductName(),
            itemRequest.getQuantity(),
            itemRequest.getUnitPrice()
        );
    }
}
