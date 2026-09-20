package com.cth.job.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {

    @Value("${app.name:CTH Job Payment Platform}")
    private String appName;

    @Value("${app.auth.provider:DB}")
    private String authProvider;

    @GetMapping("/app-info")
    public ResponseEntity<Map<String, String>> getAppInfo() {
        return ResponseEntity.ok(Map.of(
                "appName", appName,
                "authProvider", authProvider,
                "version", "1.0.0-SNAPSHOT",
                "javaVersion", System.getProperty("java.version")
        ));
    }
}
