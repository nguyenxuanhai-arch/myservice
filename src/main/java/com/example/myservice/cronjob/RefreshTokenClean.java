package com.example.myservice.cronjob;

import com.example.myservice.modules.users.repositories.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

public class RefreshTokenClean {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenClean.class);

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredTokens() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        int deleteCount = refreshTokenRepository.deleteByExpiryDateBefore(currentDateTime);
        logger.info("Đã xoá" + deleteCount + " token");
    }
}
