package com.zedd.rpl.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
    ErrorCode code = e.getErrorCode();
    return ResponseEntity.status(code.getStatus())
        .body(new ErrorResponse(LocalDateTime.now(), code.getStatus().value(),
            code.name(), e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
        .map(f -> f.getField() + ": " + f.getDefaultMessage())
        .collect(Collectors.joining("; "));
    ErrorCode code = ErrorCode.VALIDATION_ERROR;
    return ResponseEntity.status(code.getStatus())
        .body(new ErrorResponse(LocalDateTime.now(), code.getStatus().value(), code.name(), message));
  }

  /** Safety net for database constraint violations that slip past service validation. */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException e) {
    ErrorCode code = ErrorCode.DATA_CONFLICT;
    return ResponseEntity.status(code.getStatus())
        .body(new ErrorResponse(LocalDateTime.now(), code.getStatus().value(),
            code.name(), code.getDefaultMessage()));
  }
}
