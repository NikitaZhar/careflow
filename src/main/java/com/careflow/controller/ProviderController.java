package com.careflow.controller;

import com.careflow.controller.auth.SessionUserValidator;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProviderController {

    private final SessionUserValidator sessionValidator;

    public ProviderController(SessionUserValidator sessionValidator) {
        this.sessionValidator = sessionValidator;
    }

    @GetMapping("/provider")
    public String showProviderPage(HttpSession session, Model model) {
        User user = sessionValidator.getValidUser(session, UserRole.PROVIDER);
        if (user == null) return "redirect:/auth/login";

        model.addAttribute("currentUser", user);
        return "provider";
    }
}