package com.careflow.controller;

import com.careflow.controller.auth.SessionUserValidator;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientController {

    private static final String LOGIN_REDIRECT = "redirect:/auth/login";
    private static final String CLIENT_VIEW = "client";

    private final SessionUserValidator sessionValidator;

    public ClientController(SessionUserValidator sessionValidator) {
        this.sessionValidator = sessionValidator;
    }

    @GetMapping("/client")
    public String showClientPage(HttpSession session, Model model) {
        User client = getClientOrNull(session);
        if (client == null) {
            return LOGIN_REDIRECT;
        }

        populateClientModel(model, client);
        return CLIENT_VIEW;
    }

    private User getClientOrNull(HttpSession session) {
        return sessionValidator.getValidUser(session, UserRole.CLIENT);
    }

    private void populateClientModel(Model model, User client) {
        model.addAttribute("currentUser", client);
    }
}