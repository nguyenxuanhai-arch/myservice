package com.example.myservice.modules.users.requests;
import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Data
public class BlacklistTokenRequest {
    @NotBlank(message = "Token khong duoc de trong")
    private String token;
}
