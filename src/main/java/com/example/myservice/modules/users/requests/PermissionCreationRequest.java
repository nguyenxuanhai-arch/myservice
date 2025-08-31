package com.example.myservice.modules.users.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionCreationRequest {
    @NotBlank(message = "Tên không đươợc để trống")
    private String name;
    @NotBlank(message = "Mổ tả không được để trống")
    private String description;
}
