package com.waregang.receiving_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor

@Profile("dev")

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;

    @Override
    public void run(String... args) throws Exception {
        seed();
    }

    private void seed() throws IOException {
        Integer asnCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM asn", Integer.class);

        if (asnCount != null && asnCount > 0) {
            log.info(">>> Database already contains seed data. Skipping DatabaseSeeder. <<<");
            return;
        }

        Resource resource = resourceLoader.getResource("classpath:db/seed/seed.sql");
        String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        jdbcTemplate.execute(sql);

        log.info(">>> Seed is successfully executed <<<");
    }
}