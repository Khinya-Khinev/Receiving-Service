package com.waregang.receiving_service.common.exception_handling;


import com.waregang.receiving_service.common.idempotency.IdempotencyInterceptor;
import com.waregang.receiving_service.common.idempotency.IdempotencyService;
import com.waregang.receiving_service.common.idempotency.WebMvcConfig;
import com.waregang.receiving_service.receiving_process.api.GoodsReceiptController;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.application.GoodsReceiptService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@ActiveProfiles("test")
@Import(WebExceptionHandlingTestConfig.class)
@WebMvcTest(controllers = GoodsReceiptController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = WebMvcConfig.class // excluding IdempotencyInterceptor
        ))
@WithMockUser(authorities = "BOX_MANAGER")
public class ExceptionHandlingIT {

    @MockitoBean private GoodsReceiptService receiptService;
    @MockitoBean private IdempotencyService idempotencyService;
    @MockitoBean IdempotencyInterceptor idempotencyInterceptor;

    @Autowired MockMvcTester mockMvcTester;
    @Autowired JsonMapper jsonMapper;

    private static final String URL = "/api/goods-receipts";

    @Test
    @DisplayName("Should return 400 with validation error")
    void shouldReturn400ForInvalidBody() {
        String json = givenStartReceivingJsonWithInvalidParams();

        var body = assertThat(mockMvcTester.post().with(csrf())
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson();

        body.extractingPath("$.type").isEqualTo("about:blank");
        body.extractingPath("$.title").isEqualTo("Bad request");
        body.extractingPath("$.status").isEqualTo(400);
        body.extractingPath("$.detail").isEqualTo("Validation Error");
        body.extractingPath("$.instance").isEqualTo(URL);

        body.extractingPath("$.invalid_params").asMap().hasSize(2);
        body.extractingPath("$.invalid_params.asnNumber").isEqualTo("must not be blank");

        body.extractingPath("$.timestamp").isNotNull();
    }

    private String givenStartReceivingJsonWithInvalidParams() {
        StartReceivingRequest request = new StartReceivingRequest(
                null,
                "");
        return jsonMapper.writeValueAsString(request);
    }
}
