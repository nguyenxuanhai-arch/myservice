package com.example.myservice.modules.users.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResource {
    private Long id;
    private String name;
    private Integer priority;
    private Set<PermissionResource> permissions;
}
