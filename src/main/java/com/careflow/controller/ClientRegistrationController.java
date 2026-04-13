package com.careflow.controller;

import com.careflow.model.ClientInvitation;
import com.careflow.service.ClientRegistrationService;
import com.careflow.service.ClientRegistrationService.RegistrationResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClientRegistrationController {

    private final ClientRegistrationService clientRegistrationService;

    public ClientRegistrationController(ClientRegistrationService clientRegistrationService) {
        this.clientRegistrationService = clientRegistrationService;
    }

    @GetMapping("/client/register")
    public String showRegistrationPage(@RequestParam String token, Model model) {
        ClientInvitation invitation = clientRegistrationService.findValidInvitation(token);

        if (invitation == null) {
            model.addAttribute("error", "Приглашение не найдено или уже использовано");
            return "client-register-error";
        }

        model.addAttribute("token", token);
        model.addAttribute("email", invitation.getEmail());
        model.addAttribute("providerName", invitation.getProvider().getUsername());
        return "client-register";
    }

    @PostMapping("/client/register")
    public String registerClient(@RequestParam String token,
                                 @RequestParam String username,
                                 @RequestParam String password,
                                 Model model) {
        ClientInvitation invitation = clientRegistrationService.findValidInvitation(token);

        if (invitation == null) {
            model.addAttribute("error", "Приглашение не найдено или уже использовано");
            return "client-register-error";
        }

        RegistrationResult result = clientRegistrationService.registerClient(token, username, password);

        if (!result.success()) {
            model.addAttribute("token", token);
            model.addAttribute("email", invitation.getEmail());
            model.addAttribute("providerName", invitation.getProvider().getUsername());
            model.addAttribute("error", result.message());
            return "client-register";
        }

        model.addAttribute("message", result.message());
        return "client-register-success";
    }
}