package com.careflow.controller;

import com.careflow.model.ProviderInvitation;
import com.careflow.service.ProviderRegistrationService;
import com.careflow.service.ProviderRegistrationService.RegistrationResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProviderRegistrationController {

    private final ProviderRegistrationService providerRegistrationService;

    public ProviderRegistrationController(ProviderRegistrationService providerRegistrationService) {
        this.providerRegistrationService = providerRegistrationService;
    }

    @GetMapping("/provider/register")
    public String showRegistrationPage(@RequestParam String token, Model model) {
        ProviderInvitation invitation = providerRegistrationService.findValidInvitation(token);

        if (invitation == null) {
            model.addAttribute("error", "Приглашение не найдено или уже использовано");
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

        ProviderInvitation invitation = providerRegistrationService.findValidInvitation(token);

        if (invitation == null) {
            model.addAttribute("error", "Приглашение не найдено или уже использовано");
            return "provider-register-error";
        }

        RegistrationResult result = providerRegistrationService.registerProvider(token, username, password);

        if (!result.success()) {
            model.addAttribute("token", token);
            model.addAttribute("email", invitation.getEmail());
            model.addAttribute("error", result.message());
            return "provider-register";
        }

        model.addAttribute("message", result.message());
        return "provider-register-success";
    }
}