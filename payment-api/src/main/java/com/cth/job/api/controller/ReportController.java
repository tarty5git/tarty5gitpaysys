package com.cth.job.api.controller;

import com.cth.job.service.report.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummaryReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String provider) {
        Map<String, Object> report = reportService.generateTransactionSummaryReport(startDate, endDate, provider);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceReport() {
        Map<String, Object> report = reportService.generateGatewayPerformanceReport();
        return ResponseEntity.ok(report);
    }
}
