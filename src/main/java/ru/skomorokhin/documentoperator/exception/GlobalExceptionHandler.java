package ru.skomorokhin.documentoperator.exception;

import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {

        ErrorCode code = ex.getErrorCode();

        ApiError error = new ApiError(
                code.name(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, code.getStatus());
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(OptimisticLockException ex) {

        ApiError error = new ApiError(
                "CONCURRENT_MODIFICATION",
                "Concurrent modification detected",
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {

        ApiError error = new ApiError(
                "DATA_INTEGRITY_VIOLATION",
                "Database constraint violation",
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleCommon(Exception ex) {

        ApiError error = new ApiError(
                ErrorCode.INTERNAL_ERROR.name(),
                ErrorCode.INTERNAL_ERROR.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(
                error,
                ErrorCode.INTERNAL_ERROR.getStatus()
        );
    }
}
