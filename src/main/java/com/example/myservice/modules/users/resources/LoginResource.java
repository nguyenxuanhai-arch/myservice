package com.example.myservice.modules.users.resources;

public class LoginResource {
    private final String token;
    private final UserResource user;

    public LoginResource(String token, UserResource user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public UserResource getUser() {
        return user;
    }

}