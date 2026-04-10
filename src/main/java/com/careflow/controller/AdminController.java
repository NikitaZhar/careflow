package com.careflow.controller;

import com.careflow.controller.auth.SessionUserValidator;
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

    private static final String LOGIN_REDIRECT = "redirect:/auth/login";
    private static final String ADMIN_VIEW = "admin";

    private final AdminService adminService;
    private final SessionUserValidator sessionValidator;

    public AdminController(AdminService adminService,
                           SessionUserValidator sessionValidator) {
        this.adminService = adminService;
        this.sessionValidator = sessionValidator;
    }

    @GetMapping("/admin")
    public String showAdminPage(HttpSession session, Model model) {
        User admin = getAdminOrNull(session);
        if (admin == null) {
            return LOGIN_REDIRECT;
        }

        return renderAdminPage(model, admin, null, null);
    }

    @PostMapping("/admin/providers/invite")
    public String inviteProvider(@RequestParam String email,
                                 HttpSession session,
                                 Model model) {
        User admin = getAdminOrNull(session);
        if (admin == null) {
            return LOGIN_REDIRECT;
        }

        boolean created = adminService.createProviderInvitation(email);

        if (created) {
            return renderAdminPage(model, admin, null, "Приглашение создано");
        }

        return renderAdminPage(
                model,
                admin,
                "Приглашение уже существует или пользователь с таким email уже зарегистрирован",
                null
        );
    }

    private User getAdminOrNull(HttpSession session) {
        return sessionValidator.getValidUser(session, UserRole.ADMIN);
    }

    private String renderAdminPage(Model model,
                                   User admin,
                                   String error,
                                   String message) {
        populateAdminModel(model, admin);

        if (error != null) {
            model.addAttribute("error", error);
        }

        if (message != null) {
            model.addAttribute("message", message);
        }

        return ADMIN_VIEW;
    }

    private void populateAdminModel(Model model, User admin) {
        model.addAttribute("currentUser", admin);
        model.addAttribute("providers", adminService.getProviders());
    }
}