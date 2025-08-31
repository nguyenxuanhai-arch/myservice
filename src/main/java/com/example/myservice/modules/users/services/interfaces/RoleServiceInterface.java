package com.example.myservice.modules.users.services.interfaces;

import com.example.myservice.modules.users.entities.Role;
import com.example.myservice.modules.users.requests.Role.StoreRequest;
import com.example.myservice.modules.users.requests.Role.UpdateRequest;
import org.springframework.data.domain.Page;
import java.util.Map;

public interface RoleServiceInterface {
    Role create(StoreRequest request);
    Role update(Long id, UpdateRequest request);
    Page<Role> paginate(Map<String, String[]> parameters);
}
