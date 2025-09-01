package com.example.myservice.modules.users.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Set;

@RequiredArgsConstructor
@Builder
@Data
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResource {
    private final Long id;
    private final String email;
    private final String name;
    private final String phone;
    private final Set<String> roles;
    private final Set<String> permissions;

}