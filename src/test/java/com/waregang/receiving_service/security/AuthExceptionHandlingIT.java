package com.waregang.receiving_service.security;

import com.waregang.receiving_service.user.api.dto.AuthenticationRequest;
import com.waregang.receiving_service.security.application.AuthService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@Disabled

@SpringBootTest
//@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthExceptionHandlingIT {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("Should return Problem Detail for unauthorized")
    void shouldReturn401when() throws Exception {
        when(authService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // WHEN & THEN
        var response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(givenJsonWithBadCredentials()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
                .getResponse();

        var jsonNode = jsonMapper.readTree(response.getContentAsString());

        assertThat(jsonNode.get("type").asText()).isEqualTo("about:blank");
        assertThat(jsonNode.get("title").asText()).isEqualTo("Authentication failed");
        assertThat(jsonNode.get("status").asInt()).isEqualTo(401);
        assertThat(jsonNode.get("detail").asText()).isEqualTo("Bad credentials");
        assertThat(jsonNode.get("instance").asText()).isEqualTo("/api/auth/login");
        assertThat(jsonNode.get("timestamp")).isNotNull();
    }

    private String givenJsonWithBadCredentials() {
        AuthenticationRequest request = new AuthenticationRequest(
                "existing@gmail.com",
                "wrong_password"
        );
        return jsonMapper.writeValueAsString(request);
    }
}