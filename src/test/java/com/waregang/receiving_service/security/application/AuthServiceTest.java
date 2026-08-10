package com.waregang.receiving_service.security.application;

import com.waregang.receiving_service.user.domain.User;
import com.waregang.receiving_service.user.infrastructure.UserRepository;
import com.waregang.receiving_service.security.api.dto.AuthenticationRequest;
import com.waregang.receiving_service.security.api.dto.AuthenticationResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authManager;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;

    @InjectMocks private AuthService authService;

    @Test
    @DisplayName("Должен успешно аутентифицировать пользователя")
    void shouldAuthenticateSuccessfully() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "password");
        Authentication authentication = mock(Authentication.class);
        User user = mock(User.class);

        when(authManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        //when(jwtService.generateToken(user)).thenReturn("token");

        // Act
        AuthenticationResponse response = authService.authenticate(request);

        // Assert
        //assertThat(response.token()).isEqualTo("token");
    }
}
