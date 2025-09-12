package com.example.myservice.modules.users.requests.Role;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.Set;

@Data
public class RolePermissionUpdationRequest {
    @NotEmpty(message = "permissionIds không được rỗng")
    private Set<Long> permissionIds;
}
