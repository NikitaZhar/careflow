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

    private final SessionUserValidator sessionValidator;

    public ClientController(SessionUserValidator sessionValidator) {
        this.sessionValidator = sessionValidator;
    }

    @GetMapping("/client")
    public String showClientPage(HttpSession session, Model model) {
        User user = sessionValidator.getValidUser(session, UserRole.CLIENT);
        if (user == null) return "redirect:/auth/login";

        model.addAttribute("currentUser", user);
        return "client";
    }
}