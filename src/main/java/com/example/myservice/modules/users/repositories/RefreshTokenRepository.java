package com.example.myservice.modules.users.repositories;

import com.example.myservice.modules.users.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    boolean existsByRefreshToken(String refreshToken);
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
