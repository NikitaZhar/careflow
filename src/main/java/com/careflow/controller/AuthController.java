package com.careflow.controller;

import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final String SESSION_USER = "user";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        User currentUser = (User) session.getAttribute(SESSION_USER);

        if (currentUser != null) {
            return redirectByRole(currentUser.getRole());
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        User authenticatedUser = authService.authenticate(username, password);

        if (authenticatedUser == null) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }

        session.setAttribute(SESSION_USER, authenticatedUser);

        return redirectByRole(authenticatedUser.getRole());
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    private String redirectByRole(UserRole role) {
        if (role == UserRole.ADMIN) {
            return "redirect:/admin";
        }

        if (role == UserRole.PROVIDER) {
            return "redirect:/provider";
        }

        if (role == UserRole.CLIENT) {
            return "redirect:/client";
        }

        return "redirect:/auth/login";
    }
}