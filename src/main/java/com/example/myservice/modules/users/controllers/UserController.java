package com.example.myservice.modules.users.controllers;

import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.mapper.UserMapper;
import com.example.myservice.modules.users.requests.Role.RolesForUserUpdationRequest;
import com.example.myservice.modules.users.requests.User.UserUpdationRequest;
import com.example.myservice.modules.users.resources.UserDetailsResource;
import com.example.myservice.modules.users.resources.UserProfileResource;
import com.example.myservice.modules.users.resources.UserResource;
import com.example.myservice.modules.users.services.interfaces.AuthServiceInterface;
import com.example.myservice.modules.users.services.interfaces.UserServiceInterface;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.myservice.resources.ApiResource;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final AuthServiceInterface authService;
    private final UserServiceInterface userService;
    private final UserMapper userMapper;

    @RequestMapping("/me")
    public ResponseEntity<?> me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info(email);

        UserDetailsResource userDetailsResource = authService.getUserFromEmail(email);
        ApiResource<UserDetailsResource> response = ApiResource.ok(userDetailsResource, "SUCCESS");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public  ResponseEntity<?> updateRolesForUser(@Valid @RequestBody RolesForUserUpdationRequest request, @PathVariable @Positive(message = "id phải lớn hơn 0") Long id) {
        try {
            UserDetailsResource resource = authService.updateRolesForUser(request.getRoleIds(), id);
            ApiResource<UserDetailsResource> response = ApiResource.ok(resource, "Cập nhật bản ghi thành công");
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

    @GetMapping("/list")
    public ResponseEntity<?> list(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        List<User> users = userService.getAll(parameterMap);
            List<UserResource> userResources = userMapper.tResourceList(users);

        ApiResource<java.util.List<UserResource>> response = ApiResource.ok(userResources, " SUCCESS");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> index(HttpServletRequest request) {
        Map<String, String[]> parameters = request.getParameterMap();
        Page<User> users = userService.paginate(parameters);

        Page<UserResource> userResources = userMapper.tResourcePage(users);

        ApiResource<?> response = ApiResource.ok(userResources,
                " SUCCESS");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> profile(@PathVariable @Positive(message = "id phải lớn hơn 0") Long id) {
        try {
            UserDetailsResource resource = userService.getById(id);
            ApiResource<UserDetailsResource> response = ApiResource.ok(resource, " SUCCESS");
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

    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserUpdationRequest request, @PathVariable @Positive(message = "id phải lớn hơn 0") Long id) {
        try {
            UserProfileResource resource = userService.update(id, request);
            ApiResource<UserProfileResource> response = ApiResource.ok(resource, "Cập nhật bản ghi thành công");
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

    @DeleteMapping({"/{id}"})
    public ResponseEntity<?> delete(@PathVariable @Positive(message = "id phải lớn hơn 0") Long id) {
        try {
            userService.delete(id);
            return ResponseEntity.ok(ApiResource.ok(null, "Xóa bản ghi thành công"));
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