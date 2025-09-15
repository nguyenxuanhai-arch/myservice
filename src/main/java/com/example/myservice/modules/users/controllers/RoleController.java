package com.example.myservice.modules.users.controllers;

import com.example.myservice.modules.users.entities.Role;
import com.example.myservice.modules.users.mapper.RoleMapper;
import com.example.myservice.modules.users.requests.Role.RoleCreationRequest;
import com.example.myservice.modules.users.requests.Role.RoleUpdationRequest;
import com.example.myservice.modules.users.requests.Role.RolePermissionUpdationRequest;
import com.example.myservice.modules.users.resources.RoleDetailsResource;
import com.example.myservice.modules.users.resources.RoleResource;
import com.example.myservice.modules.users.services.interfaces.RoleServiceInterface;
import com.example.myservice.resources.ApiResource;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping("api/v1/roles")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    private final RoleServiceInterface roleService;
    private final RoleMapper roleMapper;

    @GetMapping("/list")
    public ResponseEntity<?> list(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        List<Role> roles = roleService.getAll(parameterMap);
        List<RoleResource> roleResources = roleMapper.tResourceList(roles);

        ApiResource<List<RoleResource>> response = ApiResource.ok(roleResources, " SUCCESS");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> index(HttpServletRequest request)
    {
        Map<String, String[]> parameters = request.getParameterMap();
        Page<Role> roles = roleService.paginate(parameters);

        Page<RoleResource> roleResources = roleMapper.tResourcePage(roles);
        ApiResource<Page<RoleResource>> response = ApiResource.ok(roleResources,
                " SUCCESS");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> store(@Valid @RequestBody RoleCreationRequest request) {
        Role role = roleService.create(request);
        RoleResource roleResource = roleMapper.tResource(role);

        ApiResource<RoleResource> response = ApiResource.ok(roleResource, "Thêm bản ghi thành công");
        logger.info("Method store running...");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody RoleUpdationRequest request, @PathVariable @Positive(message = "id phải lớn hơn 0") Long id) {

        try {
            Role role = roleService.update(id, request);
            RoleResource roleResource = roleMapper.tResource(role);

            ApiResource<RoleResource> response = ApiResource.ok(roleResource, "Cập nhật bản ghi thành công");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResource.error("NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResource.error("INTERNAL_SERVER_ERROR", "Có lỗi xảy ra trong quá trình cập nhật", HttpStatus.INTERNAL_SERVER_ERROR
            ));
        }
    }

    @PutMapping("/permissions/{id}")
    public ResponseEntity<?> updatePermissionsForRole(@Valid @RequestBody RolePermissionUpdationRequest request, @PathVariable @Positive(message = "id phải lớn hơn 0") Long id) {
        try {
            RoleResource resource = roleService.updatePermissionsForRole(id, request.getPermissionIds());
            ApiResource<RoleResource> response = ApiResource.ok(resource, "Cập nhật bản ghi thành công");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResource.error("NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResource.error("INTERNAL_SERVER_ERROR", "Có lỗi xảy ra trong quá trình cập nhật", HttpStatus.INTERNAL_SERVER_ERROR
                    ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable @Positive(message = "id phải lớn hơn 0") Long id) {
        RoleDetailsResource data = roleService.findById(id);
        ApiResource<RoleDetailsResource> response = ApiResource.ok(data, "Success");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public  ResponseEntity<?> delete(@PathVariable @Positive(message = "id phải lớn hơn 0") Long id) {
        try {
            roleService.delete(id);
            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResource.builder()
                            .status(HttpStatus.OK)
                            .message("Xoá thành công vai trò với id :" + id)
                            .success(true)
                            .build()
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