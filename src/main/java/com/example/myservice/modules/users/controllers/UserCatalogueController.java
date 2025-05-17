package com.example.myservice.modules.users.controllers;

import com.example.myservice.modules.users.entities.UserCatalogue;
import com.example.myservice.modules.users.requests.UserCatalogue.StoreRequest;
import com.example.myservice.modules.users.requests.UserCatalogue.UpdateRequest;
import com.example.myservice.modules.users.resources.UserCatalogueResource;
import com.example.myservice.modules.users.services.interfaces.UserCatalogueInterface;
import com.example.myservice.resources.ApiResource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@Controller
@RequestMapping("api/v1")
public class UserCatalogueController {

    private static final Logger logger = LoggerFactory.getLogger(UserCatalogueController.class);

    private final UserCatalogueInterface userCatalogueService;

    public UserCatalogueController(UserCatalogueInterface userCatalogueInterface) {
        this.userCatalogueService = userCatalogueInterface;
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
    public ResponseEntity<?> update(@Valid @RequestBody UpdateRequest request) {

        logger.info("Method update running...");
        return ResponseEntity.ok(1231);
    }
}