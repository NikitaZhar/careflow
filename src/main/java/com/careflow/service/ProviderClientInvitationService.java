package com.careflow.service;

import com.careflow.model.ClientInvitation;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.repository.ClientInvitationRepository;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProviderClientInvitationService {

    private final ClientInvitationRepository clientInvitationRepository;
    private final UserRepository userRepository;

    public ProviderClientInvitationService(ClientInvitationRepository clientInvitationRepository,
                                           UserRepository userRepository) {
        this.clientInvitationRepository = clientInvitationRepository;
        this.userRepository = userRepository;
    }

    public boolean createClientInvitation(User provider, String email) {
        if (provider == null || provider.getId() == null || provider.getRole() != UserRole.PROVIDER) {
            return false;
        }

        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty()) {
            return false;
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            return false;
        }

        if (clientInvitationRepository.existsByEmailAndProviderIdAndUsedFalse(normalizedEmail, provider.getId())) {
            return false;
        }

        clientInvitationRepository.save(createInvitation(provider, normalizedEmail));
        return true;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private ClientInvitation createInvitation(User provider, String email) {
        ClientInvitation invitation = new ClientInvitation();
        invitation.setEmail(email);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setUsed(false);
        invitation.setProvider(provider);
        return invitation;
    }
}