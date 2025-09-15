package com.example.myservice.modules.users.services.interfaces;

import com.example.myservice.modules.users.entities.Role;
import com.example.myservice.modules.users.requests.Role.RoleCreationRequest;
import com.example.myservice.modules.users.requests.Role.RoleUpdationRequest;
import com.example.myservice.modules.users.resources.RoleDetailsResource;
import com.example.myservice.modules.users.resources.RoleResource;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RoleServiceInterface {
    List<Role> getAll(Map<String, String[]> parameters);
    Role create(RoleCreationRequest request);
    Role update(Long id, RoleUpdationRequest request);
    Page<Role> paginate(Map<String, String[]> parameters);
    RoleDetailsResource findById(Long id);
    void delete(Long id);
    RoleResource updatePermissionsForRole(Long roleId, Set<Long> permissionIds);
}
