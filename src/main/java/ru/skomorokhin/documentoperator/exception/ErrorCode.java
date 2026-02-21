package ru.skomorokhin.documentoperator.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Document not found"),

    INVALID_STATUS(HttpStatus.BAD_REQUEST, "Invalid document status"),

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation error"),

    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),

    REGISTRY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to register approval"),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
