package com.waregang.receiving_service.receiving_process.api;

import com.waregang.receiving_service.receiving_process.application.GoodsReceiptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@ActiveProfiles("test")
@Import(com.waregang.receiving_service.common.exception_handling.IntegrationMvcTestConfig.class)
@WebMvcTest(GoodsReceiptController.class)
class GoodsReceiptControllerTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private GoodsReceiptService goodsReceiptService;

    @Test
    @WithMockUser(authorities = "MANAGER")
    void startReceiving_ShouldReturnCreated() {
        mockMvcTester.post().uri("/api/receiving-service/goods-receipts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"asnNumber\":\"asn1\",\"gateNumber\":\"gate1\"}")
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.CREATED);
    }

    @Test
    @WithMockUser(authorities = "MANAGER")
    void closeReceiving_ShouldReturnOk() {
        mockMvcTester.post().uri("/api/receiving-service/goods-receipts/{receipt-id}/closure", UUID.randomUUID())
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.OK);
    }
}
