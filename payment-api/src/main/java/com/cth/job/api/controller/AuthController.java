package com.cth.job.api.controller;

import com.cth.job.core.model.User;
import com.cth.job.service.security.AuthService;
import com.cth.job.service.security.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        boolean authenticated = authService.authenticate(username, password);
        if (authenticated) {
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "username", username,
                    "provider", authService.getActiveAuthProvider(),
                    "message", "User authenticated successfully."
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", "FAILED",
                    "message", "Invalid credentials, account locked, or expired password."
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody Map<String, String> request) {
        User user = userService.createUser(
                request.get("username"),
                request.get("password"),
                request.get("email"),
                request.get("firstName"),
                request.get("lastName"),
                request.get("phoneNumber")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/users/{username}/lock")
    public ResponseEntity<Map<String, String>> lockUser(@PathVariable String username) {
        userService.lockUser(username);
        return ResponseEntity.ok(Map.of("username", username, "status", "LOCKED"));
    }

    @PostMapping("/users/{username}/unlock")
    public ResponseEntity<Map<String, String>> unlockUser(@PathVariable String username) {
        userService.unlockUser(username);
        return ResponseEntity.ok(Map.of("username", username, "status", "UNLOCKED"));
    }

    @PostMapping("/users/{username}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @PathVariable String username,
            @RequestBody Map<String, String> body) {
        userService.resetPassword(username, body.get("newPassword"));
        return ResponseEntity.ok(Map.of("username", username, "status", "PASSWORD_RESET_SUCCESSFUL"));
    }
}
