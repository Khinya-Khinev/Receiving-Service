package com.waregang.receiving_service.common.exception_handling;

import com.waregang.receiving_service.security.application.JwtService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        GlobalExceptionHandler.class,
        ProblemDetailSimpleFactory.class,
        MessageSourceConfig.class,

        JwtService.class
})
public class WebExceptionHandlingTestConfig {}