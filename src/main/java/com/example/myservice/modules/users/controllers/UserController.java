package com.example.myservice.modules.users.controllers;

import com.example.myservice.modules.users.requests.Role.RolesForUserUpdationRequest;
import com.example.myservice.modules.users.services.interfaces.AuthServiceInterface;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.myservice.modules.users.resources.UserResource;
import com.example.myservice.resources.ApiResource;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final AuthServiceInterface userService;

    @RequestMapping("/me")
    public ResponseEntity<?> me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info(email);

        UserResource userResource = userService.getUserFromEmail(email);
        ApiResource<UserResource> response = ApiResource.ok(userResource, "SUCCESS");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public  ResponseEntity<?> updateRolesForUser(@Valid @RequestBody RolesForUserUpdationRequest request, @PathVariable Long id) {
        try {
            UserResource resource = userService.updateRolesForUser(request.getRoleIds(), id);
            ApiResource<UserResource> response = ApiResource.ok(resource, "Cập nhật bản ghi thành công");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResource.error("NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResource.error("INTERNAL_SERVER_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR
                    ));
        }
    }
}