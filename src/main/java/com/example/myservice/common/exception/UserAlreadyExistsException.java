package com.example.myservice.common.exception;
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String msg) { super(msg); }
}
