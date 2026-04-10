package com.careflow;

import com.careflow.model.User;
import com.careflow.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenNoSession_thenAdminRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void whenNoSession_thenClientRedirectToLogin() throws Exception {
        mockMvc.perform(get("/client"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void whenNoSession_thenProviderRedirectToLogin() throws Exception {
        mockMvc.perform(get("/provider"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void whenAdminAccessAdminPage_thenSuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User admin = createUserWithId(1L, "admin_test", "123", "admin@test.com", UserRole.ADMIN);
        session.setAttribute("user", admin);

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("providers"));
    }

    @Test
    void whenClientAccessClientPage_thenSuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User client = createUserWithId(2L, "client_test", "123", "client@test.com", UserRole.CLIENT);
        session.setAttribute("user", client);

        mockMvc.perform(get("/client").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("client"))
                .andExpect(model().attributeExists("currentUser"));
    }

    @Test
    void whenProviderAccessProviderPage_thenSuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User provider = createUserWithId(3L, "provider_test", "123", "provider@test.com", UserRole.PROVIDER);
        session.setAttribute("user", provider);

        mockMvc.perform(get("/provider").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("provider"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("activities"));
    }

    @Test
    void whenClientAccessAdminPage_thenRedirectToLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User client = createUserWithId(4L, "client_test_2", "123", "client2@test.com", UserRole.CLIENT);
        session.setAttribute("user", client);

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    private User createUserWithId(Long id,
                                  String username,
                                  String password,
                                  String email,
                                  UserRole role) {
        User user = new User(username, password, email, role);
        setId(user, id);
        return user;
    }

    private void setId(User user, Long id) {
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось установить id пользователю", e);
        }
    }
}