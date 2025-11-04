package com.example.oms.grpc;

import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderStatus;
import com.example.oms.service.OrderService;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GrpcOrderService extends OrderServiceGrpc.OrderServiceImplBase {

    private final OrderService orderService;

    public GrpcOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void createOrder(CreateOrderRequest request, StreamObserver<Order> responseObserver) {
        try {
            List<OrderItem> items = request.getItemsList().stream()
                    .map(i -> new OrderItem(i.getProductId(), i.getQuantity()))
                    .toList();
            var created = orderService.createOrder(request.getCustomerName(), items);
            responseObserver.onNext(toProto(created.getId().toString(), created.getCustomerName(), created.getCreatedAt(), created.getStatus(), created.getItems()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to create order").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void getOrder(OrderId request, StreamObserver<Order> responseObserver) {
        try {
            UUID id = parseUuid(request.getId());
            var order = orderService.getOrder(id);
            responseObserver.onNext(toProto(order.getId().toString(), order.getCustomerName(), order.getCreatedAt(), order.getStatus(), order.getItems()));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid UUID").asRuntimeException());
        } catch (OrderService.OrderNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to get order").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void listOrders(ListOrdersRequest request, StreamObserver<ListOrdersResponse> responseObserver) {
        try {
            Optional<OrderStatus> status = request.getStatus() == com.example.oms.grpc.OrderStatus.ORDER_STATUS_UNSPECIFIED
                    ? Optional.empty()
                    : Optional.of(fromProtoStatus(request.getStatus()));
            var orders = orderService.listOrders(status);
            ListOrdersResponse resp = ListOrdersResponse.newBuilder()
                    .addAllOrders(orders.stream()
                            .map(o -> toProto(o.getId().toString(), o.getCustomerName(), o.getCreatedAt(), o.getStatus(), o.getItems()))
                            .toList())
                    .build();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to list orders").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void updateOrderStatus(UpdateOrderStatusRequest request, StreamObserver<Order> responseObserver) {
        try {
            UUID id = parseUuid(request.getId());
            var updated = orderService.updateOrderStatus(id, fromProtoStatus(request.getStatus()));
            responseObserver.onNext(toProto(updated.getId().toString(), updated.getCustomerName(), updated.getCreatedAt(), updated.getStatus(), updated.getItems()));
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
    public void cancelOrder(OrderId request, StreamObserver<Order> responseObserver) {
        try {
            UUID id = parseUuid(request.getId());
            var cancelled = orderService.cancelOrder(id);
            responseObserver.onNext(toProto(cancelled.getId().toString(), cancelled.getCustomerName(), cancelled.getCreatedAt(), cancelled.getStatus(), cancelled.getItems()));
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

    private static Order toProto(String id, String customerName, Instant createdAt, OrderStatus status, List<OrderItem> items) {
        return Order.newBuilder()
                .setId(id)
                .setCustomerName(customerName)
                .setCreatedAt(Timestamp.newBuilder().setSeconds(createdAt.getEpochSecond()).setNanos(createdAt.getNano()).build())
                .setStatus(toProtoStatus(status))
                .addAllItems(items.stream()
                        .map(i -> com.example.oms.grpc.OrderItem.newBuilder()
                                .setProductId(i.getProductId())
                                .setQuantity(i.getQuantity())
                                .build())
                        .toList())
                .build();
    }

    private static com.example.oms.grpc.OrderStatus toProtoStatus(OrderStatus status) {
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


