package com.example.myservice.modules.users.services.interfaces;

import com.example.myservice.modules.users.requests.Auth.LoginRequest;
import com.example.myservice.modules.users.requests.Auth.RegisterRequest;
import com.example.myservice.modules.users.resources.UserResource;
import jakarta.validation.Valid;

import java.util.Set;

public interface AuthServiceInterface {
    Object authenticate(LoginRequest request);
    UserResource getUserFromEmail(String email);
    Object createUser(RegisterRequest request);
    UserResource updateRolesForUser(@Valid Set<Long> roleIds, Long id);
}
