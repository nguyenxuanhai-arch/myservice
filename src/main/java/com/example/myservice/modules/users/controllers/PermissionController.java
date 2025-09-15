package com.example.myservice.modules.users.controllers;

import com.example.myservice.modules.users.entities.Permission;
import com.example.myservice.modules.users.mapper.PermissionMapper;
import com.example.myservice.modules.users.requests.Permission.PermissionCreationRequest;
import com.example.myservice.modules.users.requests.Permission.PermissionUpdationRequest;
import com.example.myservice.modules.users.resources.PermissionResource;
import com.example.myservice.modules.users.services.interfaces.PermissionServiceInterface;
import com.example.myservice.resources.ApiResource;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@Validated
@Controller
@RequestMapping("api/v1/permissions")
public class PermissionController {
    private final PermissionServiceInterface permissionService;
    private final PermissionMapper permissionMapper;

    @GetMapping("/list")
    public ResponseEntity<?> list(HttpServletRequest request) {
        Map<String, String[]> parameter = request.getParameterMap();
        List<Permission> permissions =  permissionService.getAll(parameter);
        List<PermissionResource> permissionResources = permissionMapper.tResourceList(permissions);

        ApiResource<List<PermissionResource>> resource = ApiResource.ok(permissionResources, "Success");
        return ResponseEntity.ok(resource);
    }

    @GetMapping
    public ResponseEntity<?> index(HttpServletRequest request) {
        Map<String, String[]> parameter = request.getParameterMap();
        Page<Permission> permissions =  permissionService.paginate(parameter);
        Page<PermissionResource> permissionResources = permissionMapper.tResourcePage(permissions);

        ApiResource<Page<PermissionResource>> resource = ApiResource.ok(permissionResources, "Success");
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable @Positive(message = "id phải lớn hơn 0") Long id) {
        PermissionResource data = permissionService.findById(id);

        ApiResource<PermissionResource> response = ApiResource.ok(data, "Success");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PermissionCreationRequest request) {
        Permission permission= permissionService.create(request);
        PermissionResource resource = permissionMapper.tResource(permission);

        ApiResource<PermissionResource> response = ApiResource.ok(resource, "Thêm bản ghi thành công");
        return ResponseEntity.ok(response);
     }

    @PutMapping("/{id}")
     public ResponseEntity<?> update(@PathVariable @Positive(message = "id phải lớn hơn 0") Long id, @Valid @RequestBody PermissionUpdationRequest request) {
        try {
            Permission permission = permissionService.update(id, request);
            PermissionResource resource = permissionMapper.tResource(permission);

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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable @Positive(message = "id phải lớn hơn 0") Long id) {
        try {
            permissionService.delete(id);

            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResource.ok(null, "Xoá quyền người dùng thành công với id: " + id)
            );
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
