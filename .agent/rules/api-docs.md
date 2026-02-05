---
trigger: always_on
glob:
description: Create API documentation as Postman collections from feature docs
---

# API Documentation Skill (Postman Format)

**This is Step 2** of the feature implementation workflow:
1. Feature Docs → `docs/feature-docs/<feature-name>.md`
2. ✅ **API Docs** → `docs/api-docs/<feature-name>.postman.json`
3. Implementation → actual code

---

## When to Use

Create Postman collections **AFTER** feature docs are complete and **BEFORE** implementing code.

## Input/Output

- **Input**: Feature doc from `docs/feature-docs/<feature-name>.md`
- **Output**: Postman collection at `docs/api-docs/<feature-name>.postman_collection.json`

---

## Postman Collection Structure

```json
{
  "info": {
    "name": "Feature Name API",
    "description": "API endpoints for [feature description]",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080",
      "type": "string"
    },
    {
      "key": "token",
      "value": "",
      "type": "string"
    }
  ],
  "item": [
    {
      "name": "Endpoint Name",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"field1\": \"value\",\n  \"field2\": 123\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/v1/resource",
          "host": ["{{baseUrl}}"],
          "path": ["api", "v1", "resource"]
        },
        "description": "What this endpoint does"
      },
      "response": [
        {
          "name": "Success Response",
          "status": "OK",
          "code": 200,
          "body": "{\n  \"id\": 1,\n  \"field1\": \"value\"\n}"
        },
        {
          "name": "Error - Bad Request",
          "status": "Bad Request",
          "code": 400,
          "body": "{\n  \"error\": \"Invalid request\"\n}"
        }
      ]
    }
  ]
}
```

---

## Step-by-Step Process

### Step 1: Read Feature Doc
```bash
view_file docs/feature-docs/<feature-name>.md
```

Extract:
- API endpoints table
- Request/response data models
- Authentication requirements

### Step 2: Create Postman Collection
1. Create `docs/api-docs/<feature-name>.postman_collection.json`
2. Define collection variables (`baseUrl`, `token`)
3. Add each endpoint with:
   - Method & URL
   - Headers (Content-Type, Authorization)
   - Request body with realistic example data
   - Example responses (success + error cases)

### Step 3: Organize by Folders
Group related endpoints into folders:
```json
{
  "item": [
    {
      "name": "Authentication",
      "item": [
        { "name": "Login", ... },
        { "name": "Register", ... }
      ]
    },
    {
      "name": "Orders",
      "item": [
        { "name": "Create Order", ... },
        { "name": "Get Order", ... }
      ]
    }
  ]
}
```

### Step 4: Add Pre-request Scripts (Optional)
```json
{
  "event": [
    {
      "listen": "prerequest",
      "script": {
        "exec": ["// Auto-set timestamp or generate IDs"]
      }
    }
  ]
}
```

### Step 5: Review & Proceed
After API docs are complete, proceed to **Implementation** (Step 3 of workflow).

---

## Service Reference

| Service | Port | Base Path |
|---------|------|-----------|
| API Gateway | 8080 | `http://localhost:8080` |
| User Service | 8081 | `/api/users` or `/api/v1/users` |
| Product Service | 8082 | `/api/products` or `/api/v1/products` |
| Order Service | 8083 | `/api/orders` or `/api/v1/orders` |
| Payment Service | 8084 | `/api/payments` or `/api/v1/payments` |
| Notification Service | 8085 | `/api/notifications` or `/api/v1/notifications` |

## Authentication
- JWT Bearer tokens from User Service login
- Header: `Authorization: Bearer {{token}}`
- Store token in collection variable after login

---

## Best Practices

1. **Use collection variables** for baseUrl and token
2. **Include realistic example data** in request bodies
3. **Document error responses** with status codes
4. **Group endpoints** by feature or resource
5. **Add descriptions** to each request
6. **Include example responses** for testing reference
---
