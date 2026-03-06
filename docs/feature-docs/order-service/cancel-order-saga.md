# Cancel Order (Saga Compensation)

## Purpose
Cancel an existing order and trigger compensation steps: refund payment (if captured), release reserved inventory, and emit cancellation events.

## Service
**order-service** (Port 8083 via API Gateway 8080)

## API Endpoint
| Method | Endpoint | Description |
|--------|----------|-------------|
| DELETE | `/api/v1/orders/{id}` | Cancel an order and execute compensation (refund + inventory release). |

## Flow Diagram
```mermaid
sequenceDiagram
    participant F as Frontend
    participant G as API Gateway (8080)
    participant O as Order Service (8083)
    participant P as Product Service (8082)
    participant Pay as Payment Service (8084)
    participant N as Notification Service (8085)
    participant MQ as RabbitMQ
    participant DB as Order DB (PostgreSQL)

    F->>G: DELETE /api/v1/orders/{id}
    G->>O: Forward request
    O->>DB: Load order
    alt Order cancellable
        O->>Pay: Refund payment (if PAID)
        O->>P: Release inventory holds
        O->>DB: Mark order CANCELLED
        O->>MQ: Publish OrderCancelled event
        MQ-->>N: Send cancellation notification
        O-->>G: 200 OK (cancelled)
    else Not cancellable
        O-->>G: 409 Conflict (cannot cancel in current state)
    end
```

## Acceptance Criteria
- [ ] Only cancellable states (e.g., PENDING, PAID, PROCESSING) allowed; shipped/completed return 409.
- [ ] Refund is idempotent; repeated calls do not double-refund.
- [ ] Inventory release called only once per order; safe to retry.
- [ ] OrderCancelled event emitted for downstream consumers.
- [ ] Response includes correlationId for traceability.
