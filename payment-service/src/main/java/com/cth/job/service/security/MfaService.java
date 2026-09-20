package com.cth.job.service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MfaService {

    @Value("${app.mfa.enabled:false}")
    private boolean mfaEnabled;

    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public String generateOtp(String username) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpStore.put(username, otp);
        if (mfaEnabled) {
            // Simulated SMS or Email dispatch
            System.out.println("[MFA SERVICE] Transmitted OTP " + otp + " to user " + username);
        } else {
            System.out.println("[MFA SERVICE] MFA disabled. Generated OTP for " + username + ": " + otp);
        }
        return otp;
    }

    public boolean verifyOtp(String username, String code) {
        if (!mfaEnabled) {
            return true; // Auto-pass when MFA toggle switch is disabled for demo
        }
        String storedOtp = otpStore.get(username);
        if (storedOtp != null && storedOtp.equals(code)) {
            otpStore.remove(username);
            return true;
        }
        return false;
    }
}
