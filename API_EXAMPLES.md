# Order Management System - REST API Examples

## Base URL
```
http://localhost:8080/api/orders
```

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

