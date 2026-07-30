package com.waregang.receiving_service.integration.discrepancies_report.application;

public record DiscrepancyLine(
        String sku,
        long expected,
        long actual,
        DiscrepancyType type
) {
}
