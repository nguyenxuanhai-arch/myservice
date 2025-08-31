package com.example.myservice.modules.users.controllers;

import com.example.myservice.modules.users.resources.RoleResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.myservice.modules.users.resources.UserResource;
import com.example.myservice.resources.ApiResource;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @RequestMapping("/me")
    public ResponseEntity<?> me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info(email);

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Set<RoleResource> roleResources = user.getRoles()
                .stream()
                .map(role -> RoleResource.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .priority(role.getPriority())
                        .build())
                .collect(Collectors.toSet());

        UserResource userResource = UserResource.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .roles(roleResources)
                .build();

        ApiResource<UserResource> response = ApiResource.ok(userResource, "SUCCESS");

        return ResponseEntity.ok(response);
    }
}