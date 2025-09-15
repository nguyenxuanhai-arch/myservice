package com.example.myservice.modules.users.services.impl;

import com.example.myservice.modules.users.resources.*;
import com.example.myservice.security.UserAlreadyExistsException;
import com.example.myservice.modules.users.entities.Role;
import com.example.myservice.modules.users.mapper.UserMapper;
import com.example.myservice.modules.users.repositories.RoleRepository;
import com.example.myservice.modules.users.requests.Auth.RegisterRequest;
import com.example.myservice.modules.users.services.interfaces.AuthServiceInterface;
import com.example.myservice.resources.ApiResource;
import com.example.myservice.services.BaseService;
import com.example.myservice.services.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.requests.Auth.LoginRequest;
import com.example.myservice.modules.users.repositories.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AuthService extends BaseService implements AuthServiceInterface {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    @Value("${jwt.defaultExpiration}")
    private long defaultExpiration;
    private final UserMapper userMapper;

    @Override
    public Object authenticate(LoginRequest request) {
        try {
            User user = userRepository.findByEmailWithRolesAndPermissions(
                    request.getEmail()).orElseThrow(() -> new BadCredentialsException("Email hoac mat khau khong dung"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            {
                throw new BadCredentialsException("Email hoac mat khau khong dung");
            }

            AuthResource userResource = userMapper.tAuthResource(user);

            String token = jwtService.generateToken(user.getId(), user.getEmail(), defaultExpiration);
            String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());
            return new LoginResource(token, refreshToken, userResource);

        } catch (BadCredentialsException e)
        {
            return ApiResource.error("AUTH_ERROR", e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public UserDetailsResource getUserFromEmail(String email) {
        User user = userRepository.findByEmailWithRolesAndPermissions(email).orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return userMapper.tResourceDetails(user);
    }

    @Override
    public RegisterResource createUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email đã tồn tại");
        }

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER không tồn tại trong hệ thống"));

        User user = userMapper.tEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(role));

        userRepository.save(user);
        return userMapper.tRegisterResource(user);
    }

    @Transactional
    public UserDetailsResource updateRolesForUser(Set<Long> roleIds, Long userId) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("Danh sách role rỗng");
        }

        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new EntityNotFoundException("User không tồn tại"));

        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            Set<Long> found = roles.stream().map(Role::getId).collect(Collectors.toSet());
            Set<Long> missing = new HashSet<>(roleIds); missing.removeAll(found);
            throw new EntityNotFoundException("Role không tồn tại: " + missing);
        }

        Set<Long> current = user.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
        if (current.equals(roleIds)) {
            return userMapper.tResourceDetails(user);
        }

        user.getRoles().clear();
        user.getRoles().addAll(roles);

        userRepository.save(user);

        return userMapper.tResourceDetails(user);
    }
}