package com.example.myservice.modules.users.services.interfaces;

import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.requests.User.UserUpdationRequest;
import com.example.myservice.modules.users.resources.UserProfileResource;
import org.springframework.data.domain.Page;
import java.util.Map;

public interface UserServiceInterface {
    UserProfileResource update(Long id, UserUpdationRequest user);
    void delete(Long id);
    Page<User> paginate(Map<String, String[]> parameters);
}
