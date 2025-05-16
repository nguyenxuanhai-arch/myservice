package com.example.myservice.cronjob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.myservice.modules.users.repositories.BlacklistedTokenRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class BlacklistTokenClean {
    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    private static final Logger logger = LoggerFactory.getLogger(BlacklistTokenClean.class);

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredTokens() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        int deleteCount = blacklistedTokenRepository.deleteByExpiryDateBefore(currentDateTime);
        logger.info("Đã xoá" + deleteCount + " token");
    }
}
