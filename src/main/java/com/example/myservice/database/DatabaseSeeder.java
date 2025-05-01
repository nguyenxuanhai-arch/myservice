package com.example.myservice.database;

import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.example.myservice.modules.users.entities.User;
import com.example.myservice.modules.users.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        if (isTableEmpty("User")) {

            String passString = passwordEncoder.encode("password");

            User user = new User(
                    // "Nguyen Xuan Hai",
                    // "haixuan11598@gmail.com",
                    // passString,
                    // "1",
                    // "0123456789"
            );

            user.setName("Nguyen Xuan Hai");
            user.setEmail("haixuan11598@gmail.com");
            user.setPassword(passString);
            user.setPhone("0123456789");
            user.setUser_catalogue_id(String.valueOf(1L));

            userRepository.save(user);
            logger.info("User table seeded");
        }
    }

    private boolean isTableEmpty(String tableName) {
        Long count = (Long) entityManager.createQuery("SELECT COUNT(*) FROM " + tableName).getSingleResult();
        return count == 0;
    }
}