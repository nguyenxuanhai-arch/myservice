package com.example.myservice.modules.users.services.interfaces;

import com.example.myservice.modules.users.entities.Permission;
import com.example.myservice.modules.users.requests.Permission.PermissionCreationRequest;
import com.example.myservice.modules.users.requests.Permission.PermissionUpdationRequest;
import com.example.myservice.modules.users.resources.PermissionResource;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface PermissionServiceInterface {
    Permission create(PermissionCreationRequest request);
    Permission update(Long id, PermissionUpdationRequest request);
    Page<Permission> paginate(Map<String, String[]> parameters);
    void delete(Long id);
    PermissionResource findById(Long id);
    List<Permission> getAll(Map<String, String[]> parameters);
}
