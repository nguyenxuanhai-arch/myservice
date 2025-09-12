package com.example.myservice.helps;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String msg) { super(msg); }
}
