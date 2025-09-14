package com.example.myservice.modules.users.mapper;

import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.requests.User.UserUpdationRequest;
import com.example.myservice.modules.users.resources.UserProfileResource;
import com.example.myservice.modules.users.resources.UserResource;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, PermissionMapper.class})
public interface UserMapper {
    UserResource tResource(User user);

    UserProfileResource tProfileResource(User user);

    @Mapping(target = "email", source = "email")
    @Mapping(target = "name",  source = "name")
    @Mapping(target = "phone", source = "phone")
    void updateUserFromRequest(UserUpdationRequest req, @MappingTarget User user);
}
