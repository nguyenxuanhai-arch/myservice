package com.example.myservice.modules.users.services.interfaces;

import com.example.myservice.modules.users.requests.Auth.LoginRequest;
import com.example.myservice.modules.users.requests.Auth.RegisterRequest;
import com.example.myservice.modules.users.resources.UserDetailsResource;
import jakarta.validation.Valid;

import java.util.Set;

public interface AuthServiceInterface {
    Object authenticate(LoginRequest request);
    UserDetailsResource getUserFromEmail(String email);
    Object createUser(RegisterRequest request);
    UserDetailsResource updateRolesForUser(@Valid Set<Long> roleIds, Long id);
}
