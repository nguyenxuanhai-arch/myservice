package com.example.myservice.modules.users.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.myservice.modules.users.resources.UserResource;
import com.example.myservice.resources.SuccessResource;

@RestController
@RequestMapping("api/v1")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/me")
    public ResponseEntity<?> me() {
        String email = "haixuan11598@gmail.com";
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        UserResource userResource = new UserResource(
                user.getId(),
                user.getEmail(),
                user.getName()
        );

        SuccessResource<UserResource> respone = new SuccessResource<>("Succes", userResource);

        return ResponseEntity.ok(respone);
    }
}