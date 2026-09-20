package com.cth.job.service.security;

import com.cth.job.core.enums.UserStatus;
import com.cth.job.core.model.User;
import com.cth.job.core.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String username, String rawPassword, String email, String firstName, String lastName, String phoneNumber) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username, encodedPassword, email, firstName, lastName);
        user.setPhoneNumber(phoneNumber);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void lockUser(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setAccountLocked(true);
            user.setStatus(UserStatus.LOCKED);
            userRepository.save(user);
        });
    }

    @Transactional
    public void unlockUser(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        });
    }

    @Transactional
    public void resetPassword(String username, String newRawPassword) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newRawPassword));
            user.setPasswordExpiresAt(LocalDateTime.now().plusDays(90));
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        });
    }

    @Transactional
    public void recordFailedLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setAccountLocked(true);
                user.setStatus(UserStatus.LOCKED);
            }
            userRepository.save(user);
        });
    }

    public boolean isPasswordExpired(User user) {
        return user.getPasswordExpiresAt() != null && LocalDateTime.now().isAfter(user.getPasswordExpiresAt());
    }
}
