package com.careflow.controller;

import com.careflow.model.ProviderInvitation;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.repository.ProviderInvitationRepository;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProviderRegistrationController {

    private final ProviderInvitationRepository providerInvitationRepository;
    private final UserRepository userRepository;

    public ProviderRegistrationController(ProviderInvitationRepository providerInvitationRepository,
                                          UserRepository userRepository) {
        this.providerInvitationRepository = providerInvitationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/provider/register")
    public String showRegistrationPage(@RequestParam String token, Model model) {
        ProviderInvitation invitation = findValidInvitation(token, model);

        if (invitation == null) {
            return "provider-register-error";
        }

        model.addAttribute("token", token);
        model.addAttribute("email", invitation.getEmail());

        return "provider-register";
    }

    @PostMapping("/provider/register")
    public String registerProvider(@RequestParam String token,
                                   @RequestParam String username,
                                   @RequestParam String password,
                                   Model model) {
        ProviderInvitation invitation = findValidInvitation(token, model);

        if (invitation == null) {
            return "provider-register-error";
        }

        String normalizedUsername = username.trim();
        String normalizedPassword = password.trim();
        String invitationEmail = invitation.getEmail().trim().toLowerCase();

        if (normalizedUsername.isEmpty() || normalizedPassword.isEmpty()) {
            model.addAttribute("token", token);
            model.addAttribute("email", invitation.getEmail());
            model.addAttribute("error", "Username и password обязательны");
            return "provider-register";
        }

        if (userRepository.existsByUsername(normalizedUsername)) {
            model.addAttribute("token", token);
            model.addAttribute("email", invitation.getEmail());
            model.addAttribute("error", "Пользователь с таким username уже существует");
            return "provider-register";
        }

        if (userRepository.existsByEmail(invitationEmail)) {
            model.addAttribute("token", token);
            model.addAttribute("email", invitation.getEmail());
            model.addAttribute("error", "Пользователь с таким email уже существует");
            return "provider-register";
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(invitationEmail);
        user.setPassword(normalizedPassword);
        user.setRole(UserRole.PROVIDER);

        userRepository.save(user);

        invitation.setUsed(true);
        providerInvitationRepository.save(invitation);

        model.addAttribute("message", "Регистрация провайдера успешно завершена");
        return "provider-register-success";
    }

    private ProviderInvitation findValidInvitation(String token, Model model) {
        ProviderInvitation invitation = providerInvitationRepository.findByToken(token).orElse(null);

        if (invitation == null) {
            model.addAttribute("error", "Приглашение не найдено");
            return null;
        }

        if (invitation.isUsed()) {
            model.addAttribute("error", "Приглашение уже использовано");
            return null;
        }

        return invitation;
    }
}