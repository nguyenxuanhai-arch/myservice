package com.example.myservice.modules.users.services.interfaces;

import com.example.myservice.modules.users.entities.UserCatalogue;
import com.example.myservice.modules.users.requests.UserCatalogue.StoreRequest;

public interface UserCatalogueInterface {
    UserCatalogue create(StoreRequest request);
}
