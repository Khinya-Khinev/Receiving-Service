package com.waregang.receiving_service.receiving_process.api;

import com.waregang.receiving_service.common.exception_handling.IntegrationMvcTestConfig;
import com.waregang.receiving_service.receiving_process.application.ReceivingProcessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Import(IntegrationMvcTestConfig.class)

@WebMvcTest(ReceivingProcessController.class)
class ReceivingProcessControllerIT {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private ReceivingProcessService receivingProcessService;

    @Test
    @WithMockUser
    void getWorkerStatistics_ShouldReturnOk_WhenDateIsProvided() {
        UUID userId = UUID.randomUUID();

        mockMvcTester.get().uri("/api/receiving-service/receiving-sessions/statistics/{userId}", userId)
                .param("date", LocalDate.now().toString())
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk();
    }

    @Test
    @WithMockUser
    void getWorkerStatistics_ShouldReturnOk_WhenDateIsNotProvided() {
        UUID userId = UUID.randomUUID();
        mockMvcTester.get().uri("/api/receiving-service/receiving-sessions/statistics/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk();
    }
}
