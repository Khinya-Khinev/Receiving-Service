package com.waregang.receiving_service.config;

import com.waregang.receiving_service.user.api.dto.RegisterUserRequest;
import com.waregang.receiving_service.user.domain.User;
import com.waregang.receiving_service.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Slf4j
@RequiredArgsConstructor

@Profile("dev")

@Component
public class DevAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@waregang.com").isEmpty()) {
            var command = new RegisterUserRequest(
                    "Admin",
                    "0",
                    "admin@waregang.com",
                    "string"
            );

            String encodedPassword = passwordEncoder.encode("string");

            User admin = User.createAdmin(command, encodedPassword);

            userRepository.save(admin);
        }

        log.info(">>>> Created admin <<<<");
        log.info(">>>> Password: string <<<<");
        log.info(">>>> Email: admin@waregang.com <<<<");
    }
}
