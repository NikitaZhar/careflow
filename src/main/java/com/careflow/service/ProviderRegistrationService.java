package com.careflow.service;

import org.springframework.stereotype.Service;

import com.careflow.model.ProviderInvitation;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.repository.ProviderInvitationRepository;
import com.careflow.repository.UserRepository;

@Service
public class ProviderRegistrationService {

    private final ProviderInvitationRepository providerInvitationRepository;
    private final UserRepository userRepository;

    public ProviderRegistrationService(ProviderInvitationRepository providerInvitationRepository,
                                       UserRepository userRepository) {
        this.providerInvitationRepository = providerInvitationRepository;
        this.userRepository = userRepository;
    }

    public ProviderInvitation findValidInvitation(String token) {
        ProviderInvitation invitation = providerInvitationRepository.findByToken(token).orElse(null);

        if (invitation == null || invitation.isUsed()) {
            return null;
        }

        return invitation;
    }

    public RegistrationResult registerProvider(String token, String username, String password) {
        ProviderInvitation invitation = findValidInvitation(token);

        if (invitation == null) {
            return RegistrationResult.error("Приглашение не найдено или уже использовано");
        }

        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedPassword = password == null ? "" : password.trim();
        String invitationEmail = invitation.getEmail().trim().toLowerCase();

        if (normalizedUsername.isEmpty() || normalizedPassword.isEmpty()) {
            return RegistrationResult.error("Username и password обязательны");
        }

        if (userRepository.existsByUsername(normalizedUsername)) {
            return RegistrationResult.error("Пользователь с таким username уже существует");
        }

        if (userRepository.existsByEmail(invitationEmail)) {
            return RegistrationResult.error("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(invitationEmail);
        user.setPassword(normalizedPassword);
        user.setRole(UserRole.PROVIDER);

        userRepository.save(user);

        invitation.setUsed(true);
        providerInvitationRepository.save(invitation);

        return RegistrationResult.success("Регистрация провайдера успешно завершена");
    }

    public record RegistrationResult(boolean success, String message) {
        public static RegistrationResult success(String message) {
            return new RegistrationResult(true, message);
        }

        public static RegistrationResult error(String message) {
            return new RegistrationResult(false, message);
        }
    }
}