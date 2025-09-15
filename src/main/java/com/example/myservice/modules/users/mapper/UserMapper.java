package com.example.myservice.modules.users.mapper;

import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.requests.Auth.RegisterRequest;
import com.example.myservice.modules.users.requests.User.UserUpdationRequest;
import com.example.myservice.modules.users.resources.*;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, PermissionMapper.class})
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User tEntity(RegisterRequest req);
    UserDetailsResource tResourceDetails(User user);
    UserProfileResource tProfileResource(User user);
    UserResource tResource(User user);
    AuthResource tAuthResource(User user);
    RegisterResource tRegisterResource(User user);

    @Mapping(target = "email", source = "email")
    @Mapping(target = "name",  source = "name")
    @Mapping(target = "phone", source = "phone")
    void updateUserFromRequest(UserUpdationRequest req, @MappingTarget User user);

    List<UserResource> tResourceList(List<User> users);

    default Page<UserResource> tResourcePage(Page<User> users) {
        return users.map(this::tResource);
    }
}
