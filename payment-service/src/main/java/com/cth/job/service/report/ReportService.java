package com.cth.job.service.report;

import com.cth.job.core.enums.PaymentProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class ReportService {

    public Map<String, Object> generateTransactionSummaryReport(String startDate, String endDate, String providerFilter) {
        Map<String, Object> report = new HashMap<>();
        report.put("reportName", "Transaction Volume & Capacity Summary");
        report.put("generatedAt", Instant.now().toString());
        report.put("startDate", startDate != null ? startDate : "ALL");
        report.put("endDate", endDate != null ? endDate : "ALL");
        report.put("providerFilter", providerFilter != null ? providerFilter : "ALL");

        List<Map<String, Object>> summaryData = new ArrayList<>();

        for (PaymentProvider provider : PaymentProvider.values()) {
            if (providerFilter == null || providerFilter.equalsIgnoreCase("ALL") || providerFilter.equalsIgnoreCase(provider.name())) {
                summaryData.add(Map.of(
                        "provider", provider.name(),
                        "totalTransactions", 1250,
                        "successfulVolume", new BigDecimal("185400.50"),
                        "failedVolume", new BigDecimal("1200.00"),
                        "averageLatencyMs", 45
                ));
            }
        }

        report.put("data", summaryData);
        return report;
    }

    public Map<String, Object> generateGatewayPerformanceReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("reportName", "Gateway SLA & Latency Metrics");
        report.put("generatedAt", Instant.now().toString());

        report.put("metrics", List.of(
                Map.of("provider", "STRIPE", "uptime", "99.99%", "avgResponseMs", 32),
                Map.of("provider", "PAYPAL", "uptime", "99.95%", "avgResponseMs", 58),
                Map.of("provider", "RAZORPAY", "uptime", "99.98%", "avgResponseMs", 40)
        ));

        return report;
    }
}
