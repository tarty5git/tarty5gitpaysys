package com.cth.job.api.controller;

import com.cth.job.service.security.MfaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/mfa")
public class MfaController {

    private final MfaService mfaService;

    public MfaController(MfaService mfaService) {
        this.mfaService = mfaService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getMfaStatus() {
        return ResponseEntity.ok(Map.of("mfaEnabled", mfaService.isMfaEnabled()));
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleMfa(@RequestParam boolean enabled) {
        mfaService.setMfaEnabled(enabled);
        return ResponseEntity.ok(Map.of("mfaEnabled", mfaService.isMfaEnabled(), "message", "MFA status updated."));
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateOtp(@RequestParam String username) {
        String otp = mfaService.generateOtp(username);
        return ResponseEntity.ok(Map.of("username", username, "status", "OTP_GENERATED", "otp", mfaService.isMfaEnabled() ? "SUPPRESSED" : otp));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestParam String username, @RequestParam String code) {
        boolean valid = mfaService.verifyOtp(username, code);
        return ResponseEntity.ok(Map.of("username", username, "verified", valid));
    }
}
