package com.example.myservice.modules.users.controllers;


import com.example.myservice.modules.users.entities.BlacklistedToken;
import com.example.myservice.modules.users.repositories.BlacklistedTokenRepository;
import com.example.myservice.modules.users.requests.LoginRequest;
import com.example.myservice.modules.users.resources.LoginResource;
import com.example.myservice.modules.users.services.interfaces.UserServiceInterface;
import com.example.myservice.resources.ErrorResource;
import com.example.myservice.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import com.example.myservice.modules.users.requests.BlacklistTokenRequest;
import com.example.myservice.modules.users.services.impl.BlacklistedService;
import com.example.myservice.resources.MessageResource;

import java.util.Date;


@CrossOrigin(origins = "*")
@Validated
@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private final UserServiceInterface userService;

    @Autowired
    private BlacklistedService blacklistedService;

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

    @PostMapping("blacklisted_tokens")
    public ResponseEntity<?> addTokenToBlacklist(@Valid @RequestBody BlacklistTokenRequest request) {
        try {
            Object result = blacklistedService.create(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResource("NetworkError"));
        }
    }

    @GetMapping("logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String bearerToken) {
        try {
            String token = bearerToken.substring(7);
            BlacklistTokenRequest request = new BlacklistTokenRequest();
            request.setToken(token);
            Object message = blacklistedService.create(request);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResource("NetworkError"));
        }
    }
}
