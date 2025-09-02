package com.example.myservice.modules.users.resources;

import com.example.myservice.modules.users.entities.Role;
import lombok.*;
import java.util.Set;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResource {
    private  Long id;
    private  String email;
    private  String name;
    private  String phone;
    private  Set<String> roles;
}
