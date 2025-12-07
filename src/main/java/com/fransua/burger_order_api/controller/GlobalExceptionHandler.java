package com.fransua.burger_order_api.controller;

import com.fransua.burger_order_api.exception.DuplicateResourceException;
import com.fransua.burger_order_api.exception.NotFoundResourceException;
import com.fransua.burger_order_api.exception.TechnicalFailureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<String> handleDuplicateResource(DuplicateResourceException exception) {
    log.warn("Recourse duplicate attempt: {}", exception.getMessage());
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
  }

  @ExceptionHandler(NotFoundResourceException.class)
  public ResponseEntity<String> handleNotFoundResource(NotFoundResourceException exception,
      HttpServletRequest request) {
    log.warn("Resource not found attempt on URI: {}. Details: {}.",
        request.getRequestURI(),
        exception.getMessage());
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(TechnicalFailureException.class)
  public ResponseEntity<String> handleTechnicalFailure(TechnicalFailureException exception) {
    log.error("A critical server error occurred.", exception);
    return new ResponseEntity<>("Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
