package com.example.myservice.modules.users.services.impl;

import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.mapper.UserMapper;
import com.example.myservice.modules.users.repositories.UserRepository;
import com.example.myservice.modules.users.requests.User.UserUpdationRequest;
import com.example.myservice.modules.users.resources.UserProfileResource;
import com.example.myservice.modules.users.resources.UserResource;
import com.example.myservice.modules.users.services.interfaces.UserServiceInterface;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UserService implements UserServiceInterface {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserProfileResource update(Long id, UserUpdationRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        userMapper.updateUserFromRequest(request, user);
        userRepository.save(user);
        return userMapper.tProfileResource(user);
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Page<User> paginate(Map<String, String[]> parameters) {
        return null;
    }
}
