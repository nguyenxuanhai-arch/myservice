package com.example.myservice.modules.users.repositories;

import com.example.myservice.modules.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("""
           select distinct u
           from User u
           left join fetch u.roles r
           left join fetch r.permissions p
           where u.email = :email
           """)
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);
}
