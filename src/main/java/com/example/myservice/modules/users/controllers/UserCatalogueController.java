package com.example.myservice.modules.users.controllers;

import com.example.myservice.modules.users.entities.UserCatalogue;
import com.example.myservice.modules.users.requests.UserCatalogue.StoreRequest;
import com.example.myservice.modules.users.requests.UserCatalogue.UpdateRequest;
import com.example.myservice.modules.users.resources.UserCatalogueResource;
import com.example.myservice.modules.users.services.interfaces.UserCatalogueInterface;
import com.example.myservice.resources.ApiResource;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@Controller
@RequestMapping("api/v1")
public class UserCatalogueController {

    private static final Logger logger = LoggerFactory.getLogger(UserCatalogueController.class);
    private final UserCatalogueInterface userCatalogueService;
    public UserCatalogueController(UserCatalogueInterface userCatalogueInterface) {
        this.userCatalogueService = userCatalogueInterface;
    }

    @GetMapping("/user_catalogues")
    public ResponseEntity<?> index(HttpServletRequest request)
    {
        Map<String, String[]> parameters = request.getParameterMap();
        Page<UserCatalogue> userCatalogues = userCatalogueService.paginate(parameters);

        Page<UserCatalogueResource> userCatalogueResource = userCatalogues.map(userCatalogue ->
                UserCatalogueResource.builder()
                        .id(userCatalogue.getId())
                        .name(userCatalogue.getName())
                        .publish(userCatalogue.getPublish())
                        .build()
        );
        ApiResource<Page<UserCatalogueResource>> response = ApiResource.ok(userCatalogueResource,
                " SUCCESS");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user_catalogues")
    public ResponseEntity<?> store(@Valid @RequestBody StoreRequest request) {

        UserCatalogue userCatalogue = userCatalogueService.create(request);

        UserCatalogueResource userCatalogueResource = UserCatalogueResource.builder()
                .id(userCatalogue.getId())
                .name(userCatalogue.getName())
                .publish(userCatalogue.getPublish())
                .build();

        ApiResource<UserCatalogueResource> response = ApiResource.ok(userCatalogueResource, "Thêm bản ghi thành công");
        logger.info("Method store running...");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user_catalogues/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateRequest request, @PathVariable Long id) {

        try {
            UserCatalogue userCatalogue = userCatalogueService.update(id, request);

            UserCatalogueResource userCatalogueResource = UserCatalogueResource.builder()
                    .id(userCatalogue.getId())
                    .name(userCatalogue.getName())
                    .publish(userCatalogue.getPublish())
                    .build();

            ApiResource<UserCatalogueResource> response = ApiResource.ok(userCatalogueResource, "Cập nhật bản ghi thành công");
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
}