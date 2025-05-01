package com.example.myservice.modules.users.services.interfaces;

import com.example.myservice.modules.users.requests.LoginRequest;

public interface UserServiceInterface {
    Object authenticate(LoginRequest request);
}
