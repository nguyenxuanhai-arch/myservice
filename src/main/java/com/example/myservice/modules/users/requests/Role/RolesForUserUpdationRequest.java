package com.example.myservice.modules.users.requests.Role;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data

public class RolesForUserUpdationRequest {
    @NotEmpty(message = "roleIds không được để trống")
    private Set<Long> roleIds;
}
