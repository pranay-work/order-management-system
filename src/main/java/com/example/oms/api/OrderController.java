package com.example.oms.api;

import com.example.oms.api.dto.CreateOrderRequest;
import com.example.oms.api.dto.OrderItemRequest;
import com.example.oms.api.dto.OrderResponse;
import com.example.oms.api.dto.UpdateOrderStatusRequest;
import com.example.oms.model.Order;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;
import com.example.oms.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        List<OrderItem> items = request.getItems().stream()
                .map(OrderController::toModel)
                .toList();
        UUID customerId = UUID.fromString(request.getCustomerId());
        Order created = orderService.createOrder(customerId, items);
        return ResponseEntity.created(URI.create("/api/orders/" + created.getId()))
                .body(OrderResponse.from(created));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable("id") UUID id) {
        return OrderResponse.from(orderService.getOrder(id));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<OrderResponse>> list(
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction) {
        
        Page<Order> orderPage = orderService.listOrders(
            Optional.ofNullable(status), 
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
    public OrderResponse updateStatus(@PathVariable("id") UUID id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return OrderResponse.from(orderService.updateOrderStatus(id, request.getStatus()));
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable("id") UUID id) {
        return OrderResponse.from(orderService.cancelOrder(id));
    }

    private static OrderItem toModel(OrderItemRequest r) {
        return new OrderItem(r.getProductId(), r.getQuantity());
    }
}


