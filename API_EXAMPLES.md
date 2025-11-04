# Order Management System - API Examples

## REST API

### Base URL
```
http://localhost:8080/api/orders
```

---

## gRPC API

### Base Information
- **gRPC Server:** `localhost:9090`
- **Service:** `com.example.oms.grpc.OrderService`
- **Reflection:** Enabled (for grpcurl)

## 1. Create Order (POST)

```bash
curl -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "items": [
      {
        "productId": "PROD-001",
        "quantity": 5
      },
      {
        "productId": "PROD-002",
        "quantity": 3
      }
    ]
  }'
```

**Response:**
```json
{
  "id": "9fede852-2efb-40f6-8d58-c392dbbb2496",
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2025-11-04T23:47:57.717254Z",
  "status": "PENDING",
  "items": [
    {
      "productId": "PROD-001",
      "quantity": 5
    },
    {
      "productId": "PROD-002",
      "quantity": 3
    }
  ]
}
```

## 2. Get Order by ID (GET)

```bash
curl http://localhost:8080/api/orders/9fede852-2efb-40f6-8d58-c392dbbb2496
```

**Response:**
```json
{
  "id": "9fede852-2efb-40f6-8d58-c392dbbb2496",
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2025-11-04T23:47:57.717254Z",
  "status": "PENDING",
  "items": [...]
}
```

## 3. List All Orders (GET)

```bash
curl http://localhost:8080/api/orders
```

## 4. List Orders by Status (GET with query parameter)

```bash
# Get all PENDING orders
curl http://localhost:8080/api/orders?status=PENDING

# Get all PROCESSING orders
curl http://localhost:8080/api/orders?status=PROCESSING

# Get all SHIPPED orders
curl http://localhost:8080/api/orders?status=SHIPPED

# Get all DELIVERED orders
curl http://localhost:8080/api/orders?status=DELIVERED
```

**Valid status values:** `PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`

## 5. Update Order Status (PUT/PATCH)

```bash
curl -X PUT http://localhost:8080/api/orders/9fede852-2efb-40f6-8d58-c392dbbb2496/status \
  -H 'Content-Type: application/json' \
  -d '{
    "status": "SHIPPED"
  }'
```

**Response:**
```json
{
  "id": "9fede852-2efb-40f6-8d58-c392dbbb2496",
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2025-11-04T23:47:57.717254Z",
  "status": "SHIPPED",
  "items": [...]
}
```

## 6. Cancel Order (POST)

```bash
curl -X POST http://localhost:8080/api/orders/9fede852-2efb-40f6-8d58-c392dbbb2496/cancel
```

**Response:**
```json
{
  "id": "9fede852-2efb-40f6-8d58-c392dbbb2496",
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2025-11-04T23:47:57.717254Z",
  "status": "CANCELLED",
  "items": [...]
}
```

**Note:** Only PENDING orders can be cancelled. Attempting to cancel a non-PENDING order will return a 400 Bad Request error.

## Error Responses

### 404 Not Found
```bash
# Requesting non-existent order
curl http://localhost:8080/api/orders/00000000-0000-0000-0000-000000000000
```

**Response:**
```json
{
  "timestamp": "2025-11-04T23:50:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found: 00000000-0000-0000-0000-000000000000"
}
```

### 400 Bad Request (Validation Error)
```bash
# Missing required fields
curl -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "",
    "items": []
  }'
```

**Response:**
```json
{
  "timestamp": "2025-11-04T23:50:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "customerId": "must not be blank",
    "items": "must not be empty"
  }
}
```

### 400 Bad Request (Invalid Operation)
```bash
# Trying to cancel a non-PENDING order
curl -X POST http://localhost:8080/api/orders/<ORDER_ID>/cancel
```

**Response:**
```json
{
  "timestamp": "2025-11-04T23:50:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Only PENDING orders can be cancelled."
}
```

## Pretty Print with jq (Optional)

If you have `jq` installed, you can pipe the output for better formatting:

```bash
curl http://localhost:8080/api/orders | jq .
curl -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{...}' | jq .
```

## Complete Workflow Example

```bash
# 1. Create an order
ORDER_ID=$(curl -sS -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "items": [{"productId": "P1", "quantity": 2}]
  }' | jq -r '.id')

echo "Created order: $ORDER_ID"

# 2. Get the order
curl -sS http://localhost:8080/api/orders/$ORDER_ID | jq .

# 3. Update status to SHIPPED
curl -sS -X PUT http://localhost:8080/api/orders/$ORDER_ID/status \
  -H 'Content-Type: application/json' \
  -d '{"status": "SHIPPED"}' | jq .

# 4. List all orders
curl -sS http://localhost:8080/api/orders | jq .
```

---

# gRPC API Examples

## Prerequisites

Install `grpcurl` if not already installed:
```bash
# macOS
brew install grpcurl

# Linux
go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest

# Or download from: https://github.com/fullstorydev/grpcurl/releases
```

## List Available Services (Reflection)

```bash
# List all services
grpcurl -plaintext localhost:9090 list

# List methods in OrderService
grpcurl -plaintext localhost:9090 list com.example.oms.grpc.OrderService

# Describe a service
grpcurl -plaintext localhost:9090 describe com.example.oms.grpc.OrderService
```

## 1. Create Order (gRPC)

```bash
grpcurl -plaintext -d '{
  "customer_id": "550e8400-e29b-41d4-a716-446655440000",
  "items": [
    {
      "product_id": "PROD-001",
      "quantity": 5
    },
    {
      "product_id": "PROD-002",
      "quantity": 3
    }
  ]
}' localhost:9090 com.example.oms.grpc.OrderService/CreateOrder
```

**Response:**
```json
{
  "id": "cd4cb32d-3ab3-44f0-ba67-ae6f1c5c792c",
  "customer_id": "550e8400-e29b-41d4-a716-446655440000",
  "created_at": "2025-11-04T23:47:59.433268Z",
  "status": "PENDING",
  "items": [
    {
      "product_id": "PROD-001",
      "quantity": 5
    },
    {
      "product_id": "PROD-002",
      "quantity": 3
    }
  ]
}
```

## 2. Get Order by ID (gRPC)

```bash
grpcurl -plaintext -d '{
  "id": "cd4cb32d-3ab3-44f0-ba67-ae6f1c5c792c"
}' localhost:9090 com.example.oms.grpc.OrderService/GetOrder
```

**Response:**
```json
{
  "id": "cd4cb32d-3ab3-44f0-ba67-ae6f1c5c792c",
  "customer_id": "550e8400-e29b-41d4-a716-446655440000",
  "created_at": "2025-11-04T23:47:59.433268Z",
  "status": "PENDING",
  "items": [...]
}
```

## 3. List All Orders (gRPC)

```bash
# List all orders (use ORDER_STATUS_UNSPECIFIED or omit status)
grpcurl -plaintext -d '{
  "status": "ORDER_STATUS_UNSPECIFIED"
}' localhost:9090 com.example.oms.grpc.OrderService/ListOrders

# Or simply (empty request)
grpcurl -plaintext -d '{}' localhost:9090 com.example.oms.grpc.OrderService/ListOrders
```

**Response:**
```json
{
  "orders": [
    {
      "id": "...",
      "customer_id": "...",
      "created_at": "...",
      "status": "PENDING",
      "items": [...]
    },
    ...
  ]
}
```

## 4. List Orders by Status (gRPC)

```bash
# Get all PENDING orders
grpcurl -plaintext -d '{
  "status": "PENDING"
}' localhost:9090 com.example.oms.grpc.OrderService/ListOrders

# Get all PROCESSING orders
grpcurl -plaintext -d '{
  "status": "PROCESSING"
}' localhost:9090 com.example.oms.grpc.OrderService/ListOrders

# Get all SHIPPED orders
grpcurl -plaintext -d '{
  "status": "SHIPPED"
}' localhost:9090 com.example.oms.grpc.OrderService/ListOrders

# Get all DELIVERED orders
grpcurl -plaintext -d '{
  "status": "DELIVERED"
}' localhost:9090 com.example.oms.grpc.OrderService/ListOrders

# Get all CANCELLED orders
grpcurl -plaintext -d '{
  "status": "CANCELLED"
}' localhost:9090 com.example.oms.grpc.OrderService/ListOrders
```

**Valid status values:** `PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `ORDER_STATUS_UNSPECIFIED` (for all)

## 5. Update Order Status (gRPC)

```bash
grpcurl -plaintext -d '{
  "id": "cd4cb32d-3ab3-44f0-ba67-ae6f1c5c792c",
  "status": "SHIPPED"
}' localhost:9090 com.example.oms.grpc.OrderService/UpdateOrderStatus
```

**Response:**
```json
{
  "id": "cd4cb32d-3ab3-44f0-ba67-ae6f1c5c792c",
  "customer_id": "550e8400-e29b-41d4-a716-446655440000",
  "created_at": "2025-11-04T23:47:59.433268Z",
  "status": "SHIPPED",
  "items": [...]
}
```

## 6. Cancel Order (gRPC)

```bash
grpcurl -plaintext -d '{
  "id": "cd4cb32d-3ab3-44f0-ba67-ae6f1c5c792c"
}' localhost:9090 com.example.oms.grpc.OrderService/CancelOrder
```

**Response:**
```json
{
  "id": "cd4cb32d-3ab3-44f0-ba67-ae6f1c5c792c",
  "customer_id": "550e8400-e29b-41d4-a716-446655440000",
  "created_at": "2025-11-04T23:47:59.433268Z",
  "status": "CANCELLED",
  "items": [...]
}
```

**Note:** Only PENDING orders can be cancelled. Attempting to cancel a non-PENDING order will return a gRPC error with status `FAILED_PRECONDITION`.

## gRPC Error Responses

### NOT_FOUND (Order Not Found)
```bash
# Requesting non-existent order
grpcurl -plaintext -d '{
  "id": "00000000-0000-0000-0000-000000000000"
}' localhost:9090 com.example.oms.grpc.OrderService/GetOrder
```

**Error:**
```
ERROR:
  Code: NotFound
  Message: Order not found: 00000000-0000-0000-0000-000000000000
```

### INVALID_ARGUMENT (Invalid UUID)
```bash
# Invalid UUID format
grpcurl -plaintext -d '{
  "id": "invalid-uuid"
}' localhost:9090 com.example.oms.grpc.OrderService/GetOrder
```

**Error:**
```
ERROR:
  Code: InvalidArgument
  Message: Invalid UUID
```

### FAILED_PRECONDITION (Invalid Operation)
```bash
# Trying to cancel a non-PENDING order
grpcurl -plaintext -d '{
  "id": "<ORDER_ID>"
}' localhost:9090 com.example.oms.grpc.OrderService/CancelOrder
```

**Error:**
```
ERROR:
  Code: FailedPrecondition
  Message: Only PENDING orders can be cancelled.
```

## Pretty Print with jq (Optional)

Pipe grpcurl output to `jq` for better formatting:

```bash
grpcurl -plaintext -d '{}' localhost:9090 com.example.oms.grpc.OrderService/ListOrders | jq .
grpcurl -plaintext -d '{...}' localhost:9090 com.example.oms.grpc.OrderService/CreateOrder | jq .
```

## Complete Workflow Example (gRPC)

```bash
# 1. Create an order and capture ID
ORDER_ID=$(grpcurl -plaintext -d '{
  "customer_id": "550e8400-e29b-41d4-a716-446655440000",
  "items": [{"product_id": "P1", "quantity": 2}]
}' localhost:9090 com.example.oms.grpc.OrderService/CreateOrder | jq -r '.id')

echo "Created order: $ORDER_ID"

# 2. Get the order
grpcurl -plaintext -d "{\"id\": \"$ORDER_ID\"}" \
  localhost:9090 com.example.oms.grpc.OrderService/GetOrder | jq .

# 3. Update status to SHIPPED
grpcurl -plaintext -d "{
  \"id\": \"$ORDER_ID\",
  \"status\": \"SHIPPED\"
}" localhost:9090 com.example.oms.grpc.OrderService/UpdateOrderStatus | jq .

# 4. List all orders
grpcurl -plaintext -d '{"status": "ORDER_STATUS_UNSPECIFIED"}' \
  localhost:9090 com.example.oms.grpc.OrderService/ListOrders | jq .
```

## Field Name Differences (REST vs gRPC)

| REST API | gRPC API |
|----------|----------|
| `customerId` | `customer_id` |
| `productId` | `product_id` |
| `createdAt` | `created_at` |
| `id` | `id` (same) |
| `status` | `status` (same) |

---

