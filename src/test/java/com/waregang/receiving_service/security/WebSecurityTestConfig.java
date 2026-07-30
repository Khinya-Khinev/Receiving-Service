package com.waregang.receiving_service.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
@TestConfiguration
@Import({
        AuthenticationProvider.class,
})
public class WebSecurityTestConfig {

}
