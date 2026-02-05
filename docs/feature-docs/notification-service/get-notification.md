# Get Notification

## Purpose
Retrieve a single notification by ID.

## Service
**notification-service** (Port 8085)

## API Endpoint
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/notifications/{id}` | Get notification by ID |

## Flow Diagram

```mermaid
sequenceDiagram
    participant F as Frontend (3000)
    participant G as API Gateway (8080)
    participant N as Notification Service (8085)
    participant MDB as MongoDB
    
    F->>G: GET /api/v1/notifications/{id}
    G->>N: Forward request
    N->>MDB: Find notification by ID
    MDB-->>N: Notification document
    N-->>G: NotificationResponse
    G-->>F: Notification details
```

## Request Headers
| Header | Description |
|--------|-------------|
| Authorization | Bearer {JWT token} |

## Path Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| id | String | Notification ID (MongoDB ObjectId) |

## Response
```json
{
  "id": "65abc123def456",
  "userId": 456,
  "type": "EMAIL",
  "channel": "smtp",
  "recipient": "user@example.com",
  "subject": "Order Confirmation",
  "content": "<h1>Your order...</h1>",
  "status": "DELIVERED",
  "event": "ORDER_CREATED",
  "eventId": "123",
  "createdAt": "2026-02-05T10:00:00Z",
  "readAt": null
}
```

### Error Responses
| Status | Description |
|--------|-------------|
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Notification not found |

## Acceptance Criteria
- [ ] View notification details by ID
- [ ] Only owner or admin can access
