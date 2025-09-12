package com.example.myservice.modules.users.resources;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionResource {
    private Long id;
    private String name;
    private String description;
}
