package com.example.myservice.modules.users.requests.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserUpdationRequest {
    @Email(message = "Email khong hop le")
    @NotBlank(message = "Email khong duoc de trong")
    private String email;

    @NotBlank(message = "Name khong duoc de trong")
    private String name;

    @NotBlank(message = "Phone khong duoc de trong")
    private String phone;
}
