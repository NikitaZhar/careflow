package com.careflow.service;

import com.careflow.exception.UserNotFoundException;
import com.careflow.model.User;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        validateUsernameUniqueness(user.getUsername());
        validateEmailUniqueness(user.getEmail());

        return userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(Long id) {
        return findUserByIdOrThrow(id);
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = findUserByIdOrThrow(id);

        if (!existingUser.getUsername().equals(updatedUser.getUsername())) {
            validateUsernameUniqueness(updatedUser.getUsername());
        }

        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            validateEmailUniqueness(updatedUser.getEmail());
        }

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setRole(updatedUser.getRole());

        return userRepository.save(existingUser);
    }

    public void deleteUserById(Long id) {
        User existingUser = findUserByIdOrThrow(id);
        userRepository.delete(existingUser);
    }

    private User findUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private void validateUsernameUniqueness(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
    }
}