package com.waregang.receiving_service.integration.discrepancies_report.application;

public interface DiscrepanciesReportPort {
    void sendReport(DiscrepanciesReport report);
}
