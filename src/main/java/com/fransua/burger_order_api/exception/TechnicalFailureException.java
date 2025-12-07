package com.fransua.burger_order_api.exception;

public class TechnicalFailureException extends RuntimeException {

  public TechnicalFailureException(String message) {
    super(message);
  }

  public TechnicalFailureException(String message, Throwable cause) {
    super(message, cause);
  }
}
