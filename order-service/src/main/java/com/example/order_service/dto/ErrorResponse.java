package com.example.order_service.dto;

import java.time.OffsetDateTime;

public class ErrorResponse {
    private String message;
    private int status;
    private String correlationId;
    private OffsetDateTime timestamp;

    public ErrorResponse(String message, int status, String correlationId) {
        this.message = message;
        this.status = status;
        this.correlationId = correlationId;
        this.timestamp = OffsetDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
