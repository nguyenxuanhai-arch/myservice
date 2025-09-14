package com.example.myservice.modules.users.resources;

import java.util.Set;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDetailsResource {
    private Long id;
    private String name;
    private Integer priority;
    private Set<PermissionResource> permissions;
}
