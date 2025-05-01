package com.example.myservice.modules.users.services.impl;

import com.example.myservice.modules.users.services.interfaces.UserServiceInterface;
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
            UserResource userResource = new UserResource(
                    user.getId(),
                    user.getEmail(),
                    user.getName()
            );
            String token = jwtService.generateToken(user.getId(), user.getEmail());
            return new LoginResource(token, userResource);

        } catch (BadCredentialsException e)
        {
            logger.error("Loi xac thuc {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Co van de xay ra trong qua trinh xac thuc");
            errorResponse.put("details", error);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


}