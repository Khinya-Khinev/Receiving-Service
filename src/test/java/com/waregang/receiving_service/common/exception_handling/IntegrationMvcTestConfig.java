package com.waregang.receiving_service.common.exception_handling;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        GlobalExceptionHandler.class,
        ProblemDetailSimpleFactory.class,
        MessageSourceConfig.class,
})
public class IntegrationMvcTestConfig {}