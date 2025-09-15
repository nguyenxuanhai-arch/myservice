package com.example.myservice.modules.users.resources;

import lombok.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailsResource {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String address;
    private Set<RoleResource> roles;
}
