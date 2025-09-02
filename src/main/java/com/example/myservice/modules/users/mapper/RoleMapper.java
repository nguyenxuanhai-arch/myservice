package com.example.myservice.modules.users.mapper;

import com.example.myservice.modules.users.entities.Role;
import com.example.myservice.modules.users.resources.RoleResource;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {
    @BeanMapping(builder = @Builder(disableBuilder = false))
    RoleResource tResource(Role role);
}
