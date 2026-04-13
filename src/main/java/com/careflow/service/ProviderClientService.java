package com.careflow.service;

import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProviderClientService {

    private final UserRepository userRepository;

    public ProviderClientService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getClients(Long providerId) {
        return userRepository.findByProviderIdAndRole(providerId, UserRole.CLIENT);
    }
}