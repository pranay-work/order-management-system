package com.example.oms.grpc;

import com.example.oms.model.Order;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;
import com.example.oms.service.OrderService;
import com.example.oms.exception.OrderNotFoundException;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;

@Component
public class GrpcOrderService extends OrderServiceGrpc.OrderServiceImplBase {

    private final OrderService orderService;

    public GrpcOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void createOrder(CreateOrderRequest request, StreamObserver<com.example.oms.grpc.Order> responseObserver) {
        try {
            Order order = new Order();
            order.setCustomerId(UUID.fromString(request.getCustomerId()));
            order.setStatus(OrderStatus.PENDING);
            
            // Convert gRPC OrderItems to domain OrderItems
            List<OrderItem> orderItems = new ArrayList<>();
            for (com.example.oms.grpc.OrderItem item : request.getItemsList()) {
                OrderItem orderItem = new OrderItem(
                    item.getProductId(),
                    "", // productName is not in the gRPC message
                    item.getQuantity(),
                    BigDecimal.ZERO // unitPrice is not in the gRPC message
                );
                orderItems.add(orderItem);
            }
            order.setItems(orderItems);
            
            Order savedOrder = orderService.createOrder(order);

            responseObserver.onNext(toProto(savedOrder));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid customer UUID").asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to create order").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void getOrder(OrderId request, StreamObserver<com.example.oms.grpc.Order> responseObserver) {
        try {
            UUID id = parseUuid(request.getId());
            Optional<Order> order = orderService.getOrder(id);
            if (order.isPresent()) {
                responseObserver.onNext(toProto(order.get()));
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Order not found with id: " + request.getId())
                        .asRuntimeException());
            }
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid UUID").asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to get order").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void listOrders(ListOrdersRequest request, StreamObserver<ListOrdersResponse> responseObserver) {
        try {
            // Get orders based on status filter
            List<Order> orders;
            if (request.getStatus() != com.example.oms.grpc.OrderStatus.ORDER_STATUS_UNSPECIFIED) {
                OrderStatus status = OrderStatus.valueOf(request.getStatus().name());
                // Use paginated version with default page size
                Page<Order> page = orderService.listOrders(status, 0, 100, "createdAt", "desc");
                orders = page.getContent();
            } else {
                // Get all orders if no status filter is specified, with default pagination
                Page<Order> page = orderService.listOrders(0, 100, "createdAt", "desc");
                orders = page.getContent();
            }
            
            // Convert to proto orders
            List<com.example.oms.grpc.Order> protoOrders = orders.stream()
                    .map(this::toProto)
                    .toList();
                    
            // Create response with orders (pagination info not available with current implementation)
            ListOrdersResponse resp = ListOrdersResponse.newBuilder()
                    .addAllOrders(protoOrders)
                    .build();
                    
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to list orders: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void updateOrderStatus(UpdateOrderStatusRequest request, StreamObserver<com.example.oms.grpc.Order> responseObserver) {
        try {
            UUID id = parseUuid(request.getId());
            var updated = orderService.updateOrderStatus(id, fromProtoStatus(request.getStatus()))
                    .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
            responseObserver.onNext(toProto(updated));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid UUID").asRuntimeException());
        } catch (OrderService.InvalidOrderOperationException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (OrderService.OrderNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to update status").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void cancelOrder(OrderId request, StreamObserver<com.example.oms.grpc.Order> responseObserver) {
        try {
            UUID id = parseUuid(request.getId());
            var cancelled = orderService.cancelOrder(id)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
            responseObserver.onNext(toProto(cancelled));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid UUID").asRuntimeException());
        } catch (OrderService.InvalidOrderOperationException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (OrderService.OrderNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to cancel order").withCause(e).asRuntimeException());
        }
    }

    private static UUID parseUuid(String id) {
        return UUID.fromString(id);
    }

    private com.example.oms.grpc.Order toProto(Order order) {
        return toProto(
            order.getId().toString(),
            order.getCustomerId().toString(),
            order.getCreatedAt(),
            order.getStatus(),
            order.getItems()
        );
    }

    private com.example.oms.grpc.Order toProto(String id, String customerId, Instant createdAt, OrderStatus status, List<OrderItem> items) {
        com.example.oms.grpc.Order.Builder builder = com.example.oms.grpc.Order.newBuilder()
                .setId(id)
                .setCustomerId(customerId)
                .setCreatedAt(Timestamp.newBuilder().setSeconds(createdAt.getEpochSecond()).setNanos(createdAt.getNano()).build())
                .setStatus(toProtoStatus(status));
                
        if (items != null) {
            for (OrderItem item : items) {
                builder.addItems(com.example.oms.grpc.OrderItem.newBuilder()
                    .setProductId(item.getProductId())
                    .setQuantity(item.getQuantity())
                    .build());
            }
        }
        
        return builder.build();
    }

    private com.example.oms.grpc.OrderStatus toProtoStatus(OrderStatus status) {
        return switch (status) {
            case PENDING -> com.example.oms.grpc.OrderStatus.PENDING;
            case PROCESSING -> com.example.oms.grpc.OrderStatus.PROCESSING;
            case SHIPPED -> com.example.oms.grpc.OrderStatus.SHIPPED;
            case DELIVERED -> com.example.oms.grpc.OrderStatus.DELIVERED;
            case CANCELLED -> com.example.oms.grpc.OrderStatus.CANCELLED;
        };
    }

    private static OrderStatus fromProtoStatus(com.example.oms.grpc.OrderStatus status) {
        return switch (status) {
            case PENDING -> OrderStatus.PENDING;
            case PROCESSING -> OrderStatus.PROCESSING;
            case SHIPPED -> OrderStatus.SHIPPED;
            case DELIVERED -> OrderStatus.DELIVERED;
            case CANCELLED -> OrderStatus.CANCELLED;
            case ORDER_STATUS_UNSPECIFIED, UNRECOGNIZED -> OrderStatus.PENDING;
        };
    }
}


