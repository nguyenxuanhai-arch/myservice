package com.example.myservice.modules.users.repositories;

import com.example.myservice.modules.users.entities.Role;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findById(Long id);
}
