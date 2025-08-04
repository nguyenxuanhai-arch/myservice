package com.example.myservice.modules.users.repositories;

import com.example.myservice.modules.users.entities.UserCatalogue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCatalogueRepository extends JpaRepository<UserCatalogue, Long>, JpaSpecificationExecutor<UserCatalogue> {
}
