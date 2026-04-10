package com.careflow;

import com.careflow.controller.auth.SessionUserValidator;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

class SessionUserValidatorTest {

    private final SessionUserValidator sessionUserValidator = new SessionUserValidator();

    @Test
    void getCurrentUserReturnsUserFromSession() {
        MockHttpSession session = new MockHttpSession();
        User user = new User("admin", "123", "admin@test.com", UserRole.ADMIN);
        session.setAttribute("user", user);

        User result = sessionUserValidator.getCurrentUser(session);

        assertSame(user, result);
    }

    @Test
    void getValidUserReturnsNullWhenSessionIsEmpty() {
        MockHttpSession session = new MockHttpSession();

        User result = sessionUserValidator.getValidUser(session, UserRole.ADMIN);

        assertNull(result);
    }

    @Test
    void getValidUserReturnsNullWhenRoleDoesNotMatch() {
        MockHttpSession session = new MockHttpSession();
        User user = new User("client", "123", "client@test.com", UserRole.CLIENT);
        session.setAttribute("user", user);

        User result = sessionUserValidator.getValidUser(session, UserRole.ADMIN);

        assertNull(result);
    }

    @Test
    void getValidUserReturnsUserWhenRoleMatches() {
        MockHttpSession session = new MockHttpSession();
        User user = new User("admin", "123", "admin@test.com", UserRole.ADMIN);
        session.setAttribute("user", user);

        User result = sessionUserValidator.getValidUser(session, UserRole.ADMIN);

        assertSame(user, result);
    }

    @Test
    void getValidAdminReturnsUserWhenAdminMatches() {
        MockHttpSession session = new MockHttpSession();
        User user = new User("admin", "123", "admin@test.com", UserRole.ADMIN);
        session.setAttribute("user", user);

        User result = sessionUserValidator.getValidAdmin(session);

        assertSame(user, result);
    }

    @Test
    void getValidProviderReturnsUserWhenProviderMatches() {
        MockHttpSession session = new MockHttpSession();
        User user = new User("provider", "123", "provider@test.com", UserRole.PROVIDER);
        session.setAttribute("user", user);

        User result = sessionUserValidator.getValidProvider(session);

        assertSame(user, result);
    }

    @Test
    void getValidClientReturnsUserWhenClientMatches() {
        MockHttpSession session = new MockHttpSession();
        User user = new User("client", "123", "client@test.com", UserRole.CLIENT);
        session.setAttribute("user", user);

        User result = sessionUserValidator.getValidClient(session);

        assertSame(user, result);
    }

    @Test
    void saveStoresUserInSession() {
        MockHttpSession session = new MockHttpSession();
        User user = new User("admin", "123", "admin@test.com", UserRole.ADMIN);

        sessionUserValidator.save(session, user);

        assertSame(user, session.getAttribute("user"));
    }

    @Test
    void clearInvalidatesSession() {
        MockHttpSession session = new MockHttpSession();
        User user = new User("admin", "123", "admin@test.com", UserRole.ADMIN);
        session.setAttribute("user", user);

        sessionUserValidator.clear(session);

        assertTrue(session.isInvalid());
    }
}