package com.careflow.controller;

import com.careflow.controller.auth.SessionUserValidator;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {

    private final AdminService adminService;
    private final SessionUserValidator sessionValidator;

    public AdminController(AdminService adminService,
                           SessionUserValidator sessionValidator) {
        this.adminService = adminService;
        this.sessionValidator = sessionValidator;
    }

    @GetMapping("/admin")
    public String showAdminPage(HttpSession session, Model model) {
        User user = sessionValidator.getValidUser(session, UserRole.ADMIN);
        if (user == null) return "redirect:/auth/login";

        model.addAttribute("currentUser", user);
        model.addAttribute("providers", adminService.getProviders());
        return "admin";
    }

    @PostMapping("/admin/providers/invite")
    public String inviteProvider(@RequestParam String email,
                                 HttpSession session,
                                 Model model) {

        User user = sessionValidator.getValidUser(session, UserRole.ADMIN);
        if (user == null) return "redirect:/auth/login";

        boolean created = adminService.createProviderInvitation(email);

        model.addAttribute("currentUser", user);
        model.addAttribute("providers", adminService.getProviders());

        if (!created) {
            model.addAttribute("error",
                    "Приглашение уже существует или пользователь с таким email уже зарегистрирован");
        } else {
            model.addAttribute("message", "Приглашение создано");
        }

        return "admin";
    }
}