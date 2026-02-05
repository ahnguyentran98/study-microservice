# Refund Payment

## Purpose
Process a refund for a completed payment.

## Service
**payment-service** (Port 8084)

## API Endpoint
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payments/refund` | Process refund |

## Flow Diagram

```mermaid
sequenceDiagram
    participant F as Frontend (3000)
    participant G as API Gateway (8080)
    participant Pay as Payment Service (8084)
    participant Stripe as Stripe API
    participant DB as PostgreSQL
    participant MQ as RabbitMQ
    
    F->>G: POST /api/v1/payments/refund
    G->>Pay: Forward request
    Pay->>DB: Find payment by ID
    DB-->>Pay: Payment entity
    Pay->>Stripe: Process refund
    Stripe-->>Pay: Refund result
    Pay->>DB: Save refund record
    Pay->>DB: Update payment status
    Pay->>MQ: Publish PaymentRefunded event
    Pay-->>G: Refund response
    G-->>F: Refund confirmation
```

## Request Headers
| Header | Description |
|--------|-------------|
| Authorization | Bearer {JWT token} (Admin only) |

## Request Body
```json
{
  "paymentId": 789,
  "amount": 50.00,
  "reason": "Customer requested refund"
}
```

### Request Validation
| Field | Type | Validation |
|-------|------|------------|
| paymentId | Long | @NotNull |
| amount | BigDecimal | @NotNull (optional, defaults to full) |
| reason | String | Optional |

## Response
```json
{
  "id": 1,
  "paymentId": 789,
  "amount": 50.00,
  "reason": "Customer requested refund",
  "status": "SUCCESS",
  "refundTransactionId": "re_xxx",
  "createdAt": "2026-02-05T12:00:00Z"
}
```

### Error Responses
| Status | Description |
|--------|-------------|
| 400 | Invalid refund amount |
| 401 | Unauthorized |
| 403 | Forbidden (not admin) |
| 404 | Payment not found |
| 409 | Already refunded |

## RabbitMQ Event Published
```json
{
  "paymentId": 789,
  "orderId": 123,
  "refundAmount": 50.00,
  "timestamp": "2026-02-05T12:00:00Z"
}
```

## Acceptance Criteria
- [ ] Process refunds with admin authorization
- [ ] Support partial and full refunds
- [ ] Publish PaymentRefunded event
