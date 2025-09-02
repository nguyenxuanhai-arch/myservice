package com.example.myservice.modules.users.mapper;

import com.example.myservice.modules.users.entities.Permission;
import com.example.myservice.modules.users.resources.PermissionResource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResource tResource(Permission permission);
}
