package com.careflow.controller.auth;

import com.careflow.model.User;
import com.careflow.model.UserRole;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class SessionUserValidator {

    private static final String SESSION_USER = "user";

    public User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute(SESSION_USER);
    }

    public User getValidUser(HttpSession session, UserRole role) {
        User user = getCurrentUser(session);

        if (user == null || user.getRole() != role) {
            return null;
        }

        return user;
    }

    public void save(HttpSession session, User user) {
        session.setAttribute(SESSION_USER, user);
    }

    public void clear(HttpSession session) {
        session.invalidate();
    }
}