package com.waregang.receiving_service.advanced_shipping_notice.api;

import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateAsnRequest;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateAsnResponse;
import com.waregang.receiving_service.advanced_shipping_notice.application.AdvancedShippingNoticeMapper;
import com.waregang.receiving_service.advanced_shipping_notice.application.AdvancedShippingNoticeService;
import com.waregang.receiving_service.common.exception_handling.IntegrationMvcTestConfig;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@ActiveProfiles("test")
@Import(IntegrationMvcTestConfig.class)
@WebMvcTest(AdvancedShippingNoticeController.class)
class AdvancedShippingNoticeControllerTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private AdvancedShippingNoticeService advancedShippingNoticeService;

    @MockitoBean
    private AdvancedShippingNoticeMapper advancedShippingNoticeMapper;

    @Test
    @WithMockUser(authorities = "MANAGER")
    void createAsn_ShouldReturnCreated_WhenRequestIsValid() {
        when(advancedShippingNoticeService.createAsn(any(CreateAsnRequest.class)))
                .thenReturn(new CreateAsnResponse(UUID.randomUUID()));

        String jsonBody = """
                {
                  "externalId": "ext1",
                  "asnNumber": "asn1",
                  "warehouseId": "wh1",
                  "vendorName": "vendor1",
                  "expectedArrivalDate": "2026-09-27T10:00:00",
                  "unitRequests": [
                    {
                      "type": "type1",
                      "lpn": "lpn1"
                    }
                  ],
                  "contents": [
                    {
                      "parentLpn": "lpn1",
                      "sku": "sku1",
                      "quantity": 1
                    }
                  ]
                }
                """;

        mockMvcTester.post().uri("/api/receiving-service/asns")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.CREATED);
    }

    @Test
    @WithMockUser(authorities = "MANAGER")
    void getAsnsWithFilters_ShouldReturnOk() {
        mockMvcTester.get().uri("/api/receiving-service/asns/search")
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.OK);
    }

    @Test
    @WithMockUser(authorities = "MANAGER")
    void getAsn_ShouldReturnOk() {
        UUID asnId = UUID.randomUUID();
        mockMvcTester.get().uri("/api/receiving-service/asns/{asn_id}", asnId)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.OK);
    }

    @Test
    @WithMockUser(authorities = "MANAGER")
    void getAsnDetails_ShouldReturnOk() {
        UUID asnId = UUID.randomUUID();
        mockMvcTester.get().uri("/api/receiving-service/asns/{asn_id}/handling-units", asnId)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.OK);
    }
}
