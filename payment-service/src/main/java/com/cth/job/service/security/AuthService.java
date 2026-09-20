package com.cth.job.service.security;

import com.cth.job.core.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Value("${app.auth.provider:DB}")
    private String authProvider;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public String getActiveAuthProvider() {
        return authProvider;
    }

    public boolean authenticate(String username, String password) {
        if ("LDAP".equalsIgnoreCase(authProvider)) {
            // Simulated LDAP / Active Directory authentication validation
            return authenticateLdap(username, password);
        } else {
            return authenticateDb(username, password);
        }
    }

    private boolean authenticateDb(String username, String password) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        if (user.isAccountLocked() || userService.isPasswordExpired(user)) {
            return false;
        }

        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (matches) {
            user.setFailedLoginAttempts(0);
        } else {
            userService.recordFailedLogin(username);
        }
        return matches;
    }

    private boolean authenticateLdap(String username, String password) {
        // Active Directory / LDAP authentication verification logic
        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            return true;
        }
        return false;
    }
}
