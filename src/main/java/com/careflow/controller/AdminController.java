package com.careflow.controller;

import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private static final String SESSION_USER = "user";

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin")
    public String showAdminPage(HttpSession session, Model model) {
        User currentUser = getAuthorizedUser(session, UserRole.ADMIN);

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("currentUser", currentUser);
        loadProviders(model);
        return "admin";
    }

    @PostMapping("/admin/providers/invite")
    public String inviteProvider(@RequestParam String email,
                                 HttpSession session,
                                 Model model) {

        User currentUser = getAuthorizedUser(session, UserRole.ADMIN);

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        boolean created = adminService.createProviderInvitation(email);

        model.addAttribute("currentUser", currentUser);
        loadProviders(model);

        if (!created) {
            model.addAttribute("error", "Приглашение уже существует или пользователь с таким email уже зарегистрирован");
            return "admin";
        }

        model.addAttribute("message", "Приглашение создано");
        return "admin";
    }

    private void loadProviders(Model model) {
        model.addAttribute("providers", adminService.getProviders());
    }

    private User getAuthorizedUser(HttpSession session, UserRole requiredRole) {
        User currentUser = (User) session.getAttribute(SESSION_USER);

        if (currentUser == null) {
            return null;
        }

        if (currentUser.getRole() != requiredRole) {
            return null;
        }

        return currentUser;
    }
}