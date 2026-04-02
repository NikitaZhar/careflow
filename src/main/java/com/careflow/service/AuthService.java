package com.careflow.service;

import com.careflow.model.User;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        String normalizedUsername = username.trim();
        String normalizedPassword = password.trim();

        if (normalizedUsername.isEmpty() || normalizedPassword.isEmpty()) {
            return null;
        }

        return userRepository.findByUsername(normalizedUsername)
                .filter(user -> user.getPassword().equals(normalizedPassword))
                .orElse(null);
    }
}