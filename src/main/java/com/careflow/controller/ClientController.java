package com.careflow.controller;

import com.careflow.model.User;
import com.careflow.model.UserRole;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientController {

    private static final String SESSION_USER = "user";

    @GetMapping("/client")
    public String showClientPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (currentUser.getRole() != UserRole.CLIENT) {
            return "redirect:/auth/login";
        }

        model.addAttribute("currentUser", currentUser);
        return "client";
    }
}