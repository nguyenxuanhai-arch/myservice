package com.example.myservice.modules.users.requests.Role;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data

public class RolesForUserUpdationRequest {
    @NotNull(message = "roleIds phải là một mảng")
    @NotEmpty(message = "roleIds không được để trống")
    private Set<Long> roleIds;
}
