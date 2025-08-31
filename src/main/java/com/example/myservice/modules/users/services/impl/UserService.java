package com.example.myservice.modules.users.services.impl;

import com.example.myservice.modules.users.entities.Role;
import com.example.myservice.modules.users.resources.RoleResource;
import com.example.myservice.modules.users.services.interfaces.UserServiceInterface;
import com.example.myservice.resources.ApiResource;
import com.example.myservice.services.BaseService;
import com.example.myservice.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.requests.LoginRequest;
import com.example.myservice.modules.users.resources.LoginResource;
import com.example.myservice.modules.users.resources.UserResource;
import com.example.myservice.modules.users.repositories.UserRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService extends BaseService implements UserServiceInterface {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.defaultExpiration}")
    private long defaultExpiration;

    @Override
    public Object authenticate(LoginRequest request) {
        try {
            User user = userRepository.findByEmail(
                    request.getEmail()).orElseThrow(() -> new BadCredentialsException("Email hoac mat khau khong dung"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            {
                throw new BadCredentialsException("Email hoac mat khau khong dung");
            }
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


            String token = jwtService.generateToken(user.getId(), user.getEmail(), defaultExpiration);
            String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());
            return new LoginResource(token, refreshToken, userResource);

        } catch (BadCredentialsException e)
        {
            return ApiResource.error("AUTH_ERROR", e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
}