package com.banking.init;

import com.banking.entity.HUser;
import com.banking.enums.Role;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(HUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build());

            userRepository.save(HUser.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.USER)
                    .build());

            log.info("Seeded default users — admin/admin123 and user/user123");
        }
    }
}
