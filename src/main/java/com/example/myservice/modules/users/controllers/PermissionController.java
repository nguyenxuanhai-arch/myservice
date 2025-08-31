package com.example.myservice.modules.users.controllers;

import com.example.myservice.modules.users.entities.Permission;
import com.example.myservice.modules.users.requests.PermissionCreationRequest;
import com.example.myservice.modules.users.requests.PermissionUpdateRequest;
import com.example.myservice.modules.users.resources.PermissionResource;
import com.example.myservice.modules.users.services.interfaces.PermissionServiceInterface;
import com.example.myservice.resources.ApiResource;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("api/v1")
public class PermissionController {
    private final PermissionServiceInterface permissionService;

    public PermissionController(PermissionServiceInterface permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/permissions")
    public ResponseEntity<?> index(HttpServletRequest request) {
        Map<String, String[]> parameter = request.getParameterMap();
        Page<Permission> permissions =  permissionService.paginate(parameter);
        Page<PermissionResource> permissionResources = permissions.map(permission -> PermissionResource.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build());
        ApiResource<Page<PermissionResource>> resource = ApiResource.ok(permissionResources, "Success");
        return ResponseEntity.ok(resource);
     }

     @PostMapping("/permissions")
    public ResponseEntity<?> create(@Valid @RequestBody PermissionCreationRequest request) {
        Permission permission= permissionService.create(request);
        PermissionResource resource = PermissionResource.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
        ApiResource<PermissionResource> response = ApiResource.ok(resource, "Thêm bản ghi thành công");
        return ResponseEntity.ok(response);
     }

     @PutMapping("/permissions/{id}")
     public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody PermissionUpdateRequest request) {
        try {
            Permission permission = permissionService.update(id, request);
            PermissionResource resource = PermissionResource.builder()
                    .id(permission.getId())
                    .name(permission.getName())
                    .description(permission.getDescription())
                    .build();
            ApiResource<PermissionResource> response = ApiResource.ok(resource, "Cập nhật thành công quyền người dùng");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResource.error("NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResource.error("INTERNAL_SERVER_ERROR", "Có lỗi xảy ra trong quá trình cập nhật", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
     }
}
