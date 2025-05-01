package com.example.myservice.modules.users.controllers;


import com.example.myservice.modules.users.requests.LoginRequest;
import com.example.myservice.modules.users.resources.LoginResource;
import com.example.myservice.modules.users.services.interfaces.UserServiceInterface;
import com.example.myservice.resources.ErrorResource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Validated
@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private final UserServiceInterface userService;

    public AuthController(UserServiceInterface userService) {
        this.userService = userService;
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Object result = userService.authenticate(request);
        if (result instanceof LoginResource) {
            return ResponseEntity.ok((LoginResource) result);

        }

        if (result instanceof ErrorResource errorResource) {
            return ResponseEntity.unprocessableEntity().body(errorResource);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("NetworkError");
    }
}