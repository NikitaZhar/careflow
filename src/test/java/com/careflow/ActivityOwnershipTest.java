package com.careflow;

import com.careflow.model.Activity;
import com.careflow.model.ActivityStatus;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.service.ActivityScheduleOverrideService;
import com.careflow.service.ActivityScheduleRuleService;
import com.careflow.service.ActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ActivityOwnershipTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivityService activityService;

    @MockitoBean
    private ActivityScheduleRuleService activityScheduleRuleService;

    @MockitoBean
    private ActivityScheduleOverrideService activityScheduleOverrideService;

    @Test
    void whenNoSession_thenRedirectToLogin() throws Exception {
        mockMvc.perform(get("/provider/activities/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void whenProviderAccessNonExistentActivity_thenRedirectToProvider() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User provider = createUserWithId(1L, "testProvider", "123", "test@test.com", UserRole.PROVIDER);
        session.setAttribute("user", provider);

        when(activityService.findActivityById(999L)).thenReturn(null);

        mockMvc.perform(get("/provider/activities/999").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/provider"));
    }

    @Test
    void whenProviderAccessForeignActivity_thenRedirectToProvider() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User loggedInProvider = createUserWithId(1L, "owner1", "123", "owner1@test.com", UserRole.PROVIDER);
        User foreignProvider = createUserWithId(2L, "owner2", "123", "owner2@test.com", UserRole.PROVIDER);
        session.setAttribute("user", loggedInProvider);

        Activity foreignActivity = createActivityWithId(999L, foreignProvider);
        when(activityService.findActivityById(999L)).thenReturn(foreignActivity);

        mockMvc.perform(get("/provider/activities/999").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/provider"));
    }

    private User createUserWithId(Long id, String username, String password, String email, UserRole role) {
        User user = new User(username, password, email, role);
        setUserId(user, id);
        return user;
    }

    private Activity createActivityWithId(Long id, User provider) {
        Activity activity = new Activity();
        setActivityId(activity, id);
        activity.setProvider(provider);
        activity.setTitle("Massage");
        activity.setDescription("Relax massage");
        activity.setPrice(new BigDecimal("100.00"));
        activity.setDurationMinutes(60);
        activity.setStatus(ActivityStatus.ACTIVE);
        return activity;
    }

    private void setUserId(User user, Long id) {
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось установить id пользователю", e);
        }
    }

    private void setActivityId(Activity activity, Long id) {
        try {
            Field idField = Activity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(activity, id);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось установить id активности", e);
        }
    }
}