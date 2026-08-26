package com.waregang.receiving_service.receiving_process.api;

import com.waregang.receiving_service.common.exception_handling.IntegrationMvcTestConfig;
import com.waregang.receiving_service.receiving_process.api.dto.WorkerStatisticsResponse;
import com.waregang.receiving_service.receiving_process.application.ReceivingProcessService;
import com.waregang.receiving_service.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ActiveProfiles("test")
@Import(IntegrationMvcTestConfig.class)

@WebMvcTest(ReceivingProcessController.class)
class ReceivingProcessControllerIT {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private ReceivingProcessService receivingProcessService;

    @BeforeEach
    void setUpContext() {
        setSecurityContext();
    }

    private void setSecurityContext() {
        UserPrincipal principal = new UserPrincipal(
                UUID.randomUUID(),
                "test@test.com",
                "wh1",
                List.of(new SimpleGrantedAuthority("WORKER"))
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
        );
    }

    @Test
    void getWorkerStatistics_ShouldReturnStatistics_WhenDateIsProvided() {
        LocalDate date = LocalDate.now();
        WorkerStatisticsResponse mockResponse = new WorkerStatisticsResponse(10, 5);
        
        Mockito.when(receivingProcessService.getWorkerStatistics(any(), eq(date)))
                .thenReturn(mockResponse);

        mockMvcTester.get().uri("/api/receiving-service/receiving-sessions/daily-statistics")
                .param("date", date.toString())
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .bodyJson().extractingPath("$.receiptsReceived").asNumber().isEqualTo(10);
    }

    @Test
    void getWorkerStatistics_ShouldReturnStatistics_WhenDateIsNotProvided() {
        WorkerStatisticsResponse mockResponse = new WorkerStatisticsResponse(20, 10);
        
        Mockito.when(receivingProcessService.getWorkerStatistics(any(), any(LocalDate.class)))
                .thenReturn(mockResponse);

        mockMvcTester.get().uri("/api/receiving-service/receiving-sessions/daily-statistics")
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .bodyJson().extractingPath("$.receiptsReceived").asNumber().isEqualTo(20);
    }
}
