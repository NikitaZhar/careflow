package com.careflow.controller;

import com.careflow.controller.auth.SessionUserValidator;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.service.ActivityService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProviderController {

    private static final String LOGIN_REDIRECT = "redirect:/auth/login";
    private static final String PROVIDER_VIEW = "provider";

    private final SessionUserValidator sessionValidator;
    private final ActivityService activityService;

    public ProviderController(SessionUserValidator sessionValidator,
                              ActivityService activityService) {
        this.sessionValidator = sessionValidator;
        this.activityService = activityService;
    }

    @GetMapping("/provider")
    public String showProviderPage(HttpSession session, Model model) {
        User provider = getProviderOrNull(session);
        if (provider == null) {
            return LOGIN_REDIRECT;
        }

        populateProviderModel(model, provider);
        return PROVIDER_VIEW;
    }

    private User getProviderOrNull(HttpSession session) {
        return sessionValidator.getValidUser(session, UserRole.PROVIDER);
    }

    private void populateProviderModel(Model model, User provider) {
        model.addAttribute("currentUser", provider);
        model.addAttribute("activities", activityService.findActivitiesByProviderId(provider.getId()));
    }
}