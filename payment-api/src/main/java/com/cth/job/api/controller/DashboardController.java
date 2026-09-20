package com.cth.job.api.controller;

import com.cth.job.service.PaymentGatewayRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final PaymentGatewayRegistry registry;

    public DashboardController(PaymentGatewayRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        return ResponseEntity.ok(Map.of(
                "activeGateways", registry.getAvailableProviders().size(),
                "providerStatuses", registry.getMonitoringStatus(),
                "systemStatus", "HEALTHY",
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMetrics() {
        SseEmitter emitter = new SseEmitter(60000L);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            try {
                Map<String, Object> metrics = Map.of(
                        "timestamp", Instant.now().toString(),
                        "throughputTps", Math.round((Math.random() * 50 + 100) * 100.0) / 100.0,
                        "activeWorkerCount", registry.getMonitoringStatus().values().stream()
                                .mapToInt(PaymentGatewayRegistry.ProviderStatus::activeHandlerCount).sum()
                );
                emitter.send(SseEmitter.event().name("dashboard-metrics").data(metrics));
            } catch (IOException e) {
                emitter.complete();
                executor.shutdown();
            }
        }, 0, 2, TimeUnit.SECONDS);

        emitter.onCompletion(executor::shutdown);
        emitter.onTimeout(executor::shutdown);

        return emitter;
    }
}
