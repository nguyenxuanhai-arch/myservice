package com.example.myservice.modules.users.mapper;

import com.example.myservice.modules.users.entities.Role;
import com.example.myservice.modules.users.requests.Role.RoleCreationRequest;
import com.example.myservice.modules.users.resources.RoleDetailsResource;
import com.example.myservice.modules.users.resources.RoleResource;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {
    RoleResource tResource(Role role);
    RoleDetailsResource tResourceDetails(Role role);
    Role tEntity(RoleCreationRequest request);
    List<RoleResource> tResourceList(List<Role> roles);
    default Page<RoleResource> tResourcePage(Page<Role> roles) {
        return roles.map(this::tResource);
    }
}
