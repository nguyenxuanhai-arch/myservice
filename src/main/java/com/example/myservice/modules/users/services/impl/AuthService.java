package com.example.myservice.modules.users.services.impl;

import com.example.myservice.helps.UserAlreadyExistsException;
import com.example.myservice.modules.users.entities.Role;
import com.example.myservice.modules.users.mapper.UserMapper;
import com.example.myservice.modules.users.repositories.RoleRepository;
import com.example.myservice.modules.users.requests.Auth.RegisterRequest;
import com.example.myservice.modules.users.resources.AuthResource;
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
import com.example.myservice.modules.users.resources.LoginResource;
import com.example.myservice.modules.users.resources.UserResource;
import com.example.myservice.modules.users.repositories.UserRepository;

import java.util.HashSet;
import java.util.LinkedHashSet;
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

            Set<String> roleName = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            AuthResource userResource = AuthResource.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .phone(user.getPhone())
                    .roles(roleName)
                    .build();

            String token = jwtService.generateToken(user.getId(), user.getEmail(), defaultExpiration);
            String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());
            return new LoginResource(token, refreshToken, userResource);

        } catch (BadCredentialsException e)
        {
            return ApiResource.error("AUTH_ERROR", e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public UserResource getUserFromEmail(String email) {
        User user = userRepository.findByEmailWithRolesAndPermissions(email).orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.tResource(user);
    }

    @Override
    public UserResource createUser( RegisterRequest request) {
        // 1) Validate
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email đã tồn tại");
        }

        // 2) Lấy role mặc định
        Role roleUser = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER chưa được seed"));

        // 3) Tạo user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(roleUser))
                .build();

        user = userRepository.save(user);
        return UserResource.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }

    @Transactional // QUAN TRỌNG: update collection phải nằm trong TX
    public UserResource updateRolesForUser(Set<Long> roleIds, Long userId) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("Danh sách role rỗng");
        }

        // load user kèm roles để thao tác trên collection managed (tránh Lazy/Detached)
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new EntityNotFoundException("User không tồn tại"));

        // lấy roles theo id, đồng thời kiểm tra thiếu
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            Set<Long> found = roles.stream().map(Role::getId).collect(Collectors.toSet());
            Set<Long> missing = new HashSet<>(roleIds); missing.removeAll(found);
            throw new EntityNotFoundException("Role không tồn tại: " + missing);
        }

        // (tuỳ policy) chặn tự hạ quyền chính mình hoặc động tới ROLE_ADMIN
        // if (Objects.equals(user.getId(), AuthUtils.currentUserId()) && !rolesContainAdmin(roles)) ...

        // idempotent: nếu giống nhau thì return sớm, không ghi DB
        Set<Long> current = user.getRoles().stream().map(Role::getId).collect(Collectors.toSet());
        if (current.equals(roleIds)) {
            return userMapper.tResource(user); // đã có roles loaded, DTO ok
        }

        // cập nhật collection (đúng cách với Hibernate)
        user.getRoles().clear();
        user.getRoles().addAll(roles);

        // save không bắt buộc nếu entity đang managed, nhưng để chắc chắn flush FK
        userRepository.save(user);

        return userMapper.tResource(user);
    }
}