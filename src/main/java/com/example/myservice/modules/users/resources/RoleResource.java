package com.example.myservice.modules.users.resources;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResource {
    private final Long id;
    private final String name;
    private final Integer publish;
}
