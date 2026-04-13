package com.careflow.service;

import com.careflow.model.ClientInvitation;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.repository.ClientInvitationRepository;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientRegistrationService {

    private final ClientInvitationRepository clientInvitationRepository;
    private final UserRepository userRepository;

    public ClientRegistrationService(ClientInvitationRepository clientInvitationRepository,
                                     UserRepository userRepository) {
        this.clientInvitationRepository = clientInvitationRepository;
        this.userRepository = userRepository;
    }

    public ClientInvitation findValidInvitation(String token) {
        ClientInvitation invitation = clientInvitationRepository.findByToken(token).orElse(null);

        if (invitation == null || invitation.isUsed()) {
            return null;
        }

        return invitation;
    }

    public RegistrationResult registerClient(String token, String username, String password) {
        ClientInvitation invitation = findValidInvitation(token);

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

        User client = new User();
        client.setUsername(normalizedUsername);
        client.setPassword(normalizedPassword);
        client.setEmail(invitationEmail);
        client.setRole(UserRole.CLIENT);
        client.setProvider(invitation.getProvider());

        userRepository.save(client);

        invitation.setUsed(true);
        clientInvitationRepository.save(invitation);

        return RegistrationResult.success("Регистрация клиента успешно завершена");
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