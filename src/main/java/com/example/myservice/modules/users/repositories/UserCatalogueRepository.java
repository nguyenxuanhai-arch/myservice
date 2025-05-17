package com.example.myservice.modules.users.repositories;

import com.example.myservice.modules.users.entities.UserCatalogue;
import com.example.myservice.modules.users.requests.UserCatalogue.StoreRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCatalogueRepository extends JpaRepository<UserCatalogue, Long> {
    UserCatalogue findById(StoreRequest request);
}
