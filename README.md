# Order Management System

Java Spring Boot backend for a simple E-commerce order processing system.

## Build & Run

- JDK 17+
- Maven 3.9+

```bash
mvn clean test
mvn spring-boot:run
```

Server starts at http://localhost:8080

gRPC server starts at localhost:9090

## REST API

- Create order
```bash
curl -sS -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "customerName":"Alice",
    "items":[{"productId":"P1","quantity":2},{"productId":"P2","quantity":1}]
  }'
```

- Get order
```bash
curl -sS http://localhost:8080/api/orders/{id}
```

- List orders (optional status filter)
```bash
curl -sS 'http://localhost:8080/api/orders?status=PROCESSING'
```

- Update status
```bash
curl -sS -X PATCH http://localhost:8080/api/orders/{id}/status \
  -H 'Content-Type: application/json' \
  -d '{"status":"SHIPPED"}'
```

- Cancel order (only when PENDING)
```bash
curl -sS -X POST http://localhost:8080/api/orders/{id}/cancel
```

## Background Job
A scheduler runs every 5 minutes to move PENDING orders to PROCESSING.

## Notes
- In-memory storage (no external DB).
- Validation and error responses included.

## gRPC API

Proto file: `src/main/proto/order.proto`

- Create order
```bash
grpcurl -plaintext -d '{
  "customer_name":"Alice",
  "items":[{"product_id":"P1","quantity":2}]
}' localhost:9090 com.example.oms.grpc.OrderService/CreateOrder
```

- Get order
```bash
grpcurl -plaintext -d '{"id":"<ORDER_ID>"}' localhost:9090 com.example.oms.grpc.OrderService/GetOrder
```

- List orders (optional status filter)
```bash
grpcurl -plaintext -d '{"status":"PROCESSING"}' localhost:9090 com.example.oms.grpc.OrderService/ListOrders
```

- Update status
```bash
grpcurl -plaintext -d '{"id":"<ORDER_ID>","status":"SHIPPED"}' localhost:9090 com.example.oms.grpc.OrderService/UpdateOrderStatus
```

- Cancel order (only when PENDING)
```bash
grpcurl -plaintext -d '{"id":"<ORDER_ID>"}' localhost:9090 com.example.oms.grpc.OrderService/CancelOrder
```

