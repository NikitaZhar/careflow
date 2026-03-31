package com.careflow.service;

import com.careflow.exception.UserNotFoundException;
import com.careflow.model.User;
import com.careflow.repository.ActivityRepository;
import com.careflow.repository.AppointmentRepository;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final AppointmentRepository appointmentRepository;

    public UserService(UserRepository userRepository,
                       ActivityRepository activityRepository,
                       AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.appointmentRepository = appointmentRepository;
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

        if (activityRepository.existsByProviderId(id)) {
            throw new IllegalStateException("User cannot be deleted because he owns activities");
        }

        if (appointmentRepository.existsByClientId(id)) {
            throw new IllegalStateException("User cannot be deleted because he has appointments as client");
        }

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