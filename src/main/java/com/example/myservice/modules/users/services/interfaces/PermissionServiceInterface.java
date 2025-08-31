package com.example.myservice.modules.users.services.interfaces;

import com.example.myservice.modules.users.entities.Permission;
import com.example.myservice.modules.users.requests.PermissionCreationRequest;
import com.example.myservice.modules.users.requests.PermissionUpdateRequest;
import org.springframework.data.domain.Page;
import java.util.Map;

public interface PermissionServiceInterface {
    Permission create(PermissionCreationRequest request);
    Permission update(Long id, PermissionUpdateRequest request);
    Page<Permission> paginate(Map<String, String[]> parameters);
}
