package com.example.myservice.modules.users.controllers;

import com.example.myservice.modules.users.services.impl.AuthService;
import com.example.myservice.modules.users.services.interfaces.UserServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.myservice.modules.users.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.myservice.modules.users.resources.UserResource;
import com.example.myservice.resources.ApiResource;

@RestController
@RequestMapping("api/v1")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserServiceInterface userService;

    public UserController(AuthService authService) {
        this.userService = authService;
    }

    @RequestMapping("/me")
    public ResponseEntity<?> me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info(email);

        UserResource userResource = userService.getUserFromEmail(email);
        ApiResource<UserResource> response = ApiResource.ok(userResource, "SUCCESS");

        return ResponseEntity.ok(response);
    }
}