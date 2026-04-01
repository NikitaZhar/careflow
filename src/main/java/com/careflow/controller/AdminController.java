package com.careflow.controller;

import com.careflow.service.AdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin")
    public String showAdminPage(Model model) {
        loadProviders(model);
        return "admin";
    }

    @PostMapping("/admin/providers/invite")
    public String inviteProvider(@RequestParam String email, Model model) {
        boolean created = adminService.createProviderInvitation(email);

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
}