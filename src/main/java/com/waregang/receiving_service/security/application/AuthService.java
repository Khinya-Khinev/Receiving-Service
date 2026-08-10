package com.waregang.receiving_service.security.application;

import com.waregang.receiving_service.common.exception_handling.DatabaseExceptionTranslator;
import com.waregang.receiving_service.user.domain.User;
import com.waregang.receiving_service.user.infrastructure.UserRepository;
import com.waregang.receiving_service.security.api.dto.AuthenticationRequest;
import com.waregang.receiving_service.security.api.dto.AuthenticationResponse;
import com.waregang.receiving_service.security.api.dto.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.AuthenticationException;

@Slf4j

@RequiredArgsConstructor

@Service
@Validated
public class AuthService {
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final UserRepository userRepository;

    private final DatabaseExceptionTranslator databaseExceptionTranslator;

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.debug("Attempting to authenticate user: {}", request.email());
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            User user = (User) auth.getPrincipal();
            log.info("User authenticated successfully: {}", user.getEmail());

            var accessToken = jwtService.generateAccessToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);

            return new AuthenticationResponse(accessToken, refreshToken);
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}. Reason: {}", request.email(), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void registerWorker(RegisterUserRequest request) {
        log.debug("Registering new worker: {}", request.email());

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.createWorker(request, encodedPassword);

        try {
            userRepository.saveAndFlush(user);
            log.info("Worker registered successfully: {}", user.getEmail());
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to register worker: {}. Data integrity violation.", request.email());
            throw databaseExceptionTranslator.translate(e);
        }
    }

    @Transactional
    public AuthenticationResponse refresh(String refreshToken) {
        log.debug("Attempting to refresh token");

        if (!jwtService.isTokenValid(refreshToken)) {
            log.warn("Invalid refresh token attempt");
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.warn("User not found during refresh: {}", username);
                    return new RuntimeException("User not found");
                });

        log.info("Token refreshed for user: {}", user.getEmail());

        var accessToken = jwtService.generateAccessToken(user);
        var newRefreshToken = jwtService.generateRefreshToken(user);

        return new AuthenticationResponse(accessToken, newRefreshToken);
    }

    @Transactional
    public void registerManager(RegisterUserRequest request) {
        log.debug("Registering new manager: {}", request.email());
        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.createManager(request, encodedPassword);

        try {
            userRepository.saveAndFlush(user);
            log.info("Manager registered successfully: {}", user.getEmail());
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to register manager: {}. Data integrity violation.", request.email());
            throw databaseExceptionTranslator.translate(e);
        }
    }
}