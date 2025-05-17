package com.example.myservice.modules.users.services.impl;

import com.example.myservice.modules.users.entities.UserCatalogue;
import com.example.myservice.modules.users.repositories.UserCatalogueRepository;
import com.example.myservice.modules.users.requests.UserCatalogue.StoreRequest;
import com.example.myservice.modules.users.services.interfaces.UserCatalogueInterface;
import com.example.myservice.services.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserCatalogueService extends BaseService implements UserCatalogueInterface {

    @Autowired
    private UserCatalogueRepository userCatalogueRepository;

    @Override
    @Transactional
    public UserCatalogue create(StoreRequest request) {
        try {
            UserCatalogue userCatalogue = UserCatalogue.builder()
                    .name(request.getName())
                    .publish((request.getPublish()))
                    .build();
            return userCatalogueRepository.save(userCatalogue);
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed" + e.getMessage());
        }
    }
}
