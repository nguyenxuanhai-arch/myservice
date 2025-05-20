package com.example.myservice.modules.users.services.interfaces;

import com.example.myservice.modules.users.entities.UserCatalogue;
import com.example.myservice.modules.users.requests.UserCatalogue.StoreRequest;
import com.example.myservice.modules.users.requests.UserCatalogue.UpdateRequest;

public interface UserCatalogueInterface {
    UserCatalogue create(StoreRequest request);
    UserCatalogue update(Long id, UpdateRequest request);
}
