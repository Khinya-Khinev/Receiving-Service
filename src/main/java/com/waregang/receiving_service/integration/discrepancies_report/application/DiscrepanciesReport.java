package com.waregang.receiving_service.integration.discrepancies_report.application;

import java.util.List;
import java.util.UUID;

public record DiscrepanciesReport(
        UUID inboundDeliveryId,
        UUID goodsReceiptId,
        List<DiscrepancyLine> discrepancyLines
) {}
