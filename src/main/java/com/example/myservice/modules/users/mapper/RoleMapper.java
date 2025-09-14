package com.example.myservice.modules.users.mapper;

import com.example.myservice.modules.users.entities.Role;
import com.example.myservice.modules.users.requests.Role.RoleCreationRequest;
import com.example.myservice.modules.users.resources.RoleDetailsResource;
import com.example.myservice.modules.users.resources.RoleResource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {
    RoleResource tResource(Role role);
    RoleDetailsResource tResourceDetails(Role role);
    Role tEntity(RoleCreationRequest request);
}
