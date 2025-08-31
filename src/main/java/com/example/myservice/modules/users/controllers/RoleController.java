package com.example.myservice.modules.users.controllers;

import com.example.myservice.modules.users.entities.Role;
import com.example.myservice.modules.users.requests.Role.StoreRequest;
import com.example.myservice.modules.users.requests.Role.UpdateRequest;
import com.example.myservice.modules.users.resources.RoleResource;
import com.example.myservice.modules.users.services.interfaces.RoleServiceInterface;
import com.example.myservice.resources.ApiResource;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@Controller
@RequestMapping("api/v1")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    private final RoleServiceInterface roleService;
    public RoleController(RoleServiceInterface roleServiceInterface) {
        this.roleService = roleServiceInterface;
    }

    @GetMapping("/roles")
    public ResponseEntity<?> index(HttpServletRequest request)
    {
        Map<String, String[]> parameters = request.getParameterMap();
        Page<Role> userCatalogues = roleService.paginate(parameters);

        Page<RoleResource> userCatalogueResource = userCatalogues.map(role ->
                RoleResource.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .priority(role.getPriority())
                        .build()
        );
        ApiResource<Page<RoleResource>> response = ApiResource.ok(userCatalogueResource,
                " SUCCESS");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/roles")
    public ResponseEntity<?> store(@Valid @RequestBody StoreRequest request) {

        Role role = roleService.create(request);

        RoleResource roleResource = RoleResource.builder()
                .id(role.getId())
                .name(role.getName())
                .priority(role.getPriority())
                .build();

        ApiResource<RoleResource> response = ApiResource.ok(roleResource, "Thêm bản ghi thành công");
        logger.info("Method store running...");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateRequest request, @PathVariable Long id) {

        try {
            Role role = roleService.update(id, request);

            RoleResource roleResource = RoleResource.builder()
                    .id(role.getId())
                    .name(role.getName())
                    .priority(role.getPriority())
                    .build();

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

    @GetMapping("/roles/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        RoleResource data = roleService.findById(id);
        ApiResource<RoleResource> response = ApiResource.ok(data, "Success");
        return ResponseEntity.ok(response);
    }
}