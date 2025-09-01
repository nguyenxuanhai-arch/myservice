package com.example.myservice.modules.users.controllers;

import com.example.myservice.modules.users.entities.RefreshToken;
import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.repositories.RefreshTokenRepository;
import com.example.myservice.modules.users.requests.LoginRequest;
import com.example.myservice.modules.users.requests.RegisterRequest;
import com.example.myservice.modules.users.requests.RequestTokenRequest;
import com.example.myservice.modules.users.resources.LoginResource;
import com.example.myservice.modules.users.resources.RefreshTokenResource;
import com.example.myservice.modules.users.resources.UserResource;
import com.example.myservice.modules.users.services.interfaces.UserServiceInterface;
import com.example.myservice.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import com.example.myservice.modules.users.requests.BlacklistTokenRequest;
import com.example.myservice.modules.users.services.impl.BlacklistedService;
import java.util.Optional;
import java.util.logging.Logger;
import com.example.myservice.resources.ApiResource;
import com.example.myservice.resources.ApiResource.ErrorResource;

@CrossOrigin(origins = "*")
@Validated
@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private final UserServiceInterface userService;

    @Autowired
    private BlacklistedService blacklistedService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    private final Logger logger = Logger.getLogger(AuthController.class.getName());

    @Autowired
    private JwtService jwtService;

    public AuthController(UserServiceInterface userService) {
        this.userService = userService;
    }

    @PostMapping("register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        Object result = userService.createUser(registerRequest);
        if (result instanceof UserResource userResource) {
            ApiResource<UserResource> response = ApiResource.ok(userResource, "Đăng kí thành công");
            return ResponseEntity.ok(response);
        }

        if (result instanceof ApiResource errorResource) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResource);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResource("Network Error"));
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Object result = userService.authenticate(request);
        if (result instanceof LoginResource loginResource) {
            ApiResource<LoginResource> response = ApiResource.ok(loginResource, "SUCCESS");
            return ResponseEntity.ok(response);
        }

        if (result instanceof ApiResource errorResource) {
            return ResponseEntity.unprocessableEntity().body(errorResource);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResource("Network Error"));
    }

    @PostMapping("blacklisted_tokens")
    public ResponseEntity<?> addTokenToBlacklist(@Valid @RequestBody BlacklistTokenRequest request) {
        try {
            Object result = blacklistedService.create(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ApiResource<Void> errorResponse = ApiResource.<Void>builder()
                    .success(false)
                    .message("NetworkError")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String bearerToken) {
        try {
            String token = bearerToken.substring(7);
            BlacklistTokenRequest request = new BlacklistTokenRequest();
            request.setToken(token);
            blacklistedService.create(request);

            ApiResource<Void> response = ApiResource.<Void>builder()
                    .success(true)
                    .message("Đăng xuất thành công")
                    .status(HttpStatus.OK)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            ApiResource<Void> errorResponse = ApiResource.<Void>builder()
                    .success(false)
                    .message("Network Error")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RequestTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            ApiResource<Void> errorResponse = ApiResource.<Void>builder()
                    .success(false)
                    .message("Refresh Token không hợp lệ")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);

        }

        Optional<RefreshToken> dbRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken);

        if (dbRefreshToken.isPresent()) {
            Long userId = dbRefreshToken.get().getUserId();
            String email = dbRefreshToken.get().getUser().getEmail();

            String newToken = jwtService.generateToken(userId, email, null);
            String newRefreshToken =jwtService.generateRefreshToken(userId, email);

            return ResponseEntity.ok(new RefreshTokenResource(newToken, newRefreshToken));
        }

        ApiResource<Void> errorResponse = ApiResource.<Void>builder()
                .success(false)
                .message("Network Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
