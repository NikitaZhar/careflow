package com.careflow.controller;

import com.careflow.controller.auth.RoleRedirectResolver;
import com.careflow.controller.auth.SessionUserValidator;
import com.careflow.model.User;
import com.careflow.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionUserValidator sessionValidator;
    private final RoleRedirectResolver redirectResolver;

    public AuthController(AuthService authService,
                          SessionUserValidator sessionValidator,
                          RoleRedirectResolver redirectResolver) {
        this.authService = authService;
        this.sessionValidator = sessionValidator;
        this.redirectResolver = redirectResolver;
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        User user = sessionValidator.getCurrentUser(session);

        if (user != null) {
            return redirectResolver.resolve(user.getRole());
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        User user = authService.authenticate(username, password);

        if (user == null) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }

        sessionValidator.save(session, user);
        return redirectResolver.resolve(user.getRole());
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        sessionValidator.clear(session);
        return "redirect:/auth/login";
    }
}