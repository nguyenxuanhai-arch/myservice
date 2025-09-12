package com.example.myservice.modules.users.mapper;

import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.resources.UserResource;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, PermissionMapper.class})
public interface UserMapper {
    UserResource tResource(User user);
}
