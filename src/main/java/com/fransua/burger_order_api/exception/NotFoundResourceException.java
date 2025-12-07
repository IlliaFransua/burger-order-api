package com.fransua.burger_order_api.exception;

public class NotFoundResourceException extends RuntimeException {

  public NotFoundResourceException(String message) {
    super(message);
  }
}
