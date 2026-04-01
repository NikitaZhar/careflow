package com.careflow.service;

import com.careflow.model.ProviderInvitation;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.repository.ProviderInvitationRepository;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ProviderInvitationRepository providerInvitationRepository;

    public AdminService(UserRepository userRepository,
                        ProviderInvitationRepository providerInvitationRepository) {
        this.userRepository = userRepository;
        this.providerInvitationRepository = providerInvitationRepository;
    }

    public List<User> getProviders() {
        return userRepository.findByRole(UserRole.PROVIDER);
    }

    public boolean createProviderInvitation(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            return false;
        }

        if (providerInvitationRepository.existsByEmailAndUsedFalse(normalizedEmail)) {
            return false;
        }

        providerInvitationRepository.save(createInvitation(normalizedEmail));
        return true;
    }

    private ProviderInvitation createInvitation(String email) {
        ProviderInvitation invitation = new ProviderInvitation();
        invitation.setEmail(email);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setUsed(false);
        return invitation;
    }
}