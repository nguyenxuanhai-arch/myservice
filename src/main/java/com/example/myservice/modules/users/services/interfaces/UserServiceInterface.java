package com.example.myservice.modules.users.services.interfaces;

import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.requests.LoginRequest;
import com.example.myservice.modules.users.requests.RegisterRequest;
import com.example.myservice.modules.users.resources.UserResource;

public interface UserServiceInterface {
    Object authenticate(LoginRequest request);
    UserResource getUserFromEmail(String email);
    Object createUser(RegisterRequest request);
}
