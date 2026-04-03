package com.careflow.controller.auth;

import com.careflow.model.UserRole;
import org.springframework.stereotype.Component;

@Component
public class RoleRedirectResolver {

    public String resolve(UserRole role) {
        return switch (role) {
            case ADMIN -> "redirect:/admin";
            case PROVIDER -> "redirect:/provider";
            case CLIENT -> "redirect:/client";
        };
    }
}