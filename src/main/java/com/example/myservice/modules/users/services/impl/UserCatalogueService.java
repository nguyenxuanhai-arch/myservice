package com.example.myservice.modules.users.services.impl;

import com.example.myservice.modules.users.entities.UserCatalogue;
import com.example.myservice.modules.users.repositories.UserCatalogueRepository;
import com.example.myservice.modules.users.requests.UserCatalogue.StoreRequest;
import com.example.myservice.modules.users.requests.UserCatalogue.UpdateRequest;
import com.example.myservice.modules.users.services.interfaces.UserCatalogueInterface;
import com.example.myservice.services.BaseService;
import jakarta.persistence.EntityNotFoundException;
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
            UserCatalogue payload = UserCatalogue.builder()
                    .name(request.getName())
                    .publish((request.getPublish()))
                    .build();
            return userCatalogueRepository.save(payload);
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public UserCatalogue update(Long id, UpdateRequest request) {

        UserCatalogue userCatalogue = userCatalogueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nhóm thành viên không tồn tại"));

        UserCatalogue payload = userCatalogue.toBuilder()
                .name(request.getName())
                .publish(request.getPublish())
                .build();

        return userCatalogueRepository.save(payload);
    }
}
