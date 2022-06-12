package com.rxf113.springmvc2vertx.exception;

public class Springmvc2RouterException extends RuntimeException {
  public Springmvc2RouterException(String message, Throwable cause) {
    super(message, cause);
  }

  public Springmvc2RouterException(String message) {
    super(message);
  }
}

