package com.example.myservice.modules.users.mapper;

import com.example.myservice.modules.users.entities.Permission;
import com.example.myservice.modules.users.requests.Permission.PermissionCreationRequest;
import com.example.myservice.modules.users.resources.PermissionResource;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResource tResource(Permission permission);
    Permission tEntity(PermissionCreationRequest request);
    List<PermissionResource> tResourceList(List<Permission> permissions);
    default Page<PermissionResource> tResourcePage(Page<Permission> permissions) {
        return permissions.map(this::tResource);
    }
}
