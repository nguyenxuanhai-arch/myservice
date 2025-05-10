package com.example.myservice.modules.users.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestTokenRequest {
    @NotBlank(message = "RefeshToken khong duoc de trong")
    private String refeshToken;
}
