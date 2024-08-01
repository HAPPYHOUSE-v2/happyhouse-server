package com.ormi.happyhouse.member.exception;

public class WithdrawnUserException extends RuntimeException {
  public WithdrawnUserException(String message) {
    super(message);
  }
}
