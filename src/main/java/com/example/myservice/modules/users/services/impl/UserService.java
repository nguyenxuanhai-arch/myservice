package com.example.myservice.modules.users.services.impl;

import com.example.myservice.modules.users.services.interfaces.UserServiceInterface;
import com.example.myservice.resources.ErrorResource;
import com.example.myservice.services.BaseService;
import com.example.myservice.services.JwtService;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.requests.LoginRequest;
import com.example.myservice.modules.users.resources.LoginResource;
import com.example.myservice.modules.users.resources.UserResource;
import com.example.myservice.modules.users.repositories.UserRepository;

@Service
public class UserService extends BaseService implements UserServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Object authenticate(LoginRequest request) {
        try {
            User user = userRepository.findByEmail(
                    request.getEmail()).orElseThrow(() -> new BadCredentialsException("Email hoac mat khau khong dung"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            {
                throw new BadCredentialsException("Email hoac mat khau khong dung");
            }
            UserResource userResource = UserResource.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .phone(user.getPhone())
                    .build();
            String token = jwtService.generateToken(user.getId(), user.getEmail());
            String refreshToken = jwtService.generaterefreshToken(user.getId(), user.getEmail());
            return new LoginResource(token,refreshToken, userResource);

        } catch (BadCredentialsException e)
        {
            logger.error("Loi xac thuc {}", e.getMessage());

            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            ErrorResource errorResource = new ErrorResource("Co van de trong qua trinh xac thuc", error);
            return errorResource;
        }
    }


}