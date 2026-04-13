package com.careflow.controller;

import com.careflow.controller.auth.SessionUserValidator;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.service.ActivityService;
import com.careflow.service.ProviderClientInvitationService;
import com.careflow.service.ProviderClientService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProviderController {

    private static final String LOGIN_REDIRECT = "redirect:/auth/login";
    private static final String PROVIDER_VIEW = "provider";

    private final SessionUserValidator sessionValidator;
    private final ActivityService activityService;
    private final ProviderClientInvitationService providerClientInvitationService;
    private final ProviderClientService providerClientService;

    public ProviderController(SessionUserValidator sessionValidator,
                              ActivityService activityService,
                              ProviderClientInvitationService providerClientInvitationService,
                              ProviderClientService providerClientService) {
        this.sessionValidator = sessionValidator;
        this.activityService = activityService;
        this.providerClientInvitationService = providerClientInvitationService;
        this.providerClientService = providerClientService;
    }

    @GetMapping("/provider")
    public String showProviderPage(HttpSession session, Model model) {
        User provider = getProviderOrNull(session);
        if (provider == null) {
            return LOGIN_REDIRECT;
        }

        return renderProviderPage(model, provider, null, null);
    }

    @PostMapping("/provider/clients/invite")
    public String inviteClient(@RequestParam String email,
                               HttpSession session,
                               Model model) {
        User provider = getProviderOrNull(session);
        if (provider == null) {
            return LOGIN_REDIRECT;
        }

        boolean created = providerClientInvitationService.createClientInvitation(provider, email);

        if (created) {
            return renderProviderPage(model, provider, null, "Приглашение клиенту создано");
        }

        return renderProviderPage(
                model,
                provider,
                "Не удалось создать приглашение: клиент уже зарегистрирован или приглашение уже существует",
                null
        );
    }

    private User getProviderOrNull(HttpSession session) {
        return sessionValidator.getValidUser(session, UserRole.PROVIDER);
    }

    private String renderProviderPage(Model model,
                                      User provider,
                                      String error,
                                      String message) {
        model.addAttribute("currentUser", provider);
        model.addAttribute("activities", activityService.findActivitiesByProviderId(provider.getId()));
        model.addAttribute("clients", providerClientService.getClients(provider.getId()));

        if (error != null) {
            model.addAttribute("error", error);
        }

        if (message != null) {
            model.addAttribute("message", message);
        }

        return PROVIDER_VIEW;
    }
}