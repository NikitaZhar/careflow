package com.careflow;

import com.careflow.model.Activity;
import com.careflow.model.ActivityScheduleOverride;
import com.careflow.model.ActivityScheduleRule;
import com.careflow.model.ActivityStatus;
import com.careflow.model.ScheduleOverrideType;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.service.ActivityScheduleOverrideService;
import com.careflow.service.ActivitySchedulePreviewService;
import com.careflow.service.ActivityScheduleRuleService;
import com.careflow.service.ActivityService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProviderControllerScheduleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivityService activityService;

    @MockitoBean
    private ActivityScheduleRuleService activityScheduleRuleService;

    @MockitoBean
    private ActivityScheduleOverrideService activityScheduleOverrideService;

    @MockitoBean
    private ActivitySchedulePreviewService activitySchedulePreviewService;

    @Test
    void whenProviderCreatesScheduleRuleForOwnActivity_thenRedirectToActivityAndCreateRule() throws Exception {
        User provider = createUserWithId(1L, "provider1", "123", "provider1@test.com", UserRole.PROVIDER);
        Activity ownActivity = createActivityWithId(100L, provider);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", provider);

        when(activityService.findActivityById(100L)).thenReturn(ownActivity);

        mockMvc.perform(post("/provider/activities/100/schedule-rules")
                        .session(session)
                        .param("dayOfWeek", "MONDAY")
                        .param("startTime", "10:00")
                        .param("endTime", "12:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/provider/activities/100"));

        ArgumentCaptor<ActivityScheduleRule> captor =
                ArgumentCaptor.forClass(ActivityScheduleRule.class);

        verify(activityScheduleRuleService).create(captor.capture());

        ActivityScheduleRule createdRule = captor.getValue();
        assertEquals(100L, createdRule.getActivity().getId());
        assertEquals(DayOfWeek.MONDAY, createdRule.getDayOfWeek());
        assertEquals(LocalTime.of(10, 0), createdRule.getStartTime());
        assertEquals(LocalTime.of(12, 0), createdRule.getEndTime());
        assertTrue(createdRule.isActive());
    }

    @Test
    void whenProviderCreatesScheduleRuleForForeignActivity_thenRedirectToProviderAndDoNotCreateRule() throws Exception {
        User loggedInProvider = createUserWithId(1L, "provider1", "123", "provider1@test.com", UserRole.PROVIDER);
        User foreignProvider = createUserWithId(2L, "provider2", "123", "provider2@test.com", UserRole.PROVIDER);
        Activity foreignActivity = createActivityWithId(200L, foreignProvider);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", loggedInProvider);

        when(activityService.findActivityById(200L)).thenReturn(foreignActivity);

        mockMvc.perform(post("/provider/activities/200/schedule-rules")
                        .session(session)
                        .param("dayOfWeek", "TUESDAY")
                        .param("startTime", "09:00")
                        .param("endTime", "11:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/provider"));

        verify(activityScheduleRuleService, never()).create(any(ActivityScheduleRule.class));
    }

    @Test
    void whenProviderCreatesScheduleOverrideForOwnActivity_thenRedirectToActivityAndCreateOverride() throws Exception {
        User provider = createUserWithId(1L, "provider1", "123", "provider1@test.com", UserRole.PROVIDER);
        Activity ownActivity = createActivityWithId(300L, provider);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", provider);

        when(activityService.findActivityById(300L)).thenReturn(ownActivity);

        mockMvc.perform(post("/provider/activities/300/schedule-overrides")
                        .session(session)
                        .param("date", "2026-04-20")
                        .param("startTime", "15:00")
                        .param("endTime", "16:00")
                        .param("type", "AVAILABLE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/provider/activities/300"));

        ArgumentCaptor<ActivityScheduleOverride> captor =
                ArgumentCaptor.forClass(ActivityScheduleOverride.class);

        verify(activityScheduleOverrideService).create(captor.capture());

        ActivityScheduleOverride createdOverride = captor.getValue();
        assertEquals(300L, createdOverride.getActivity().getId());
        assertEquals(LocalDate.of(2026, 4, 20), createdOverride.getDate());
        assertEquals(LocalTime.of(15, 0), createdOverride.getStartTime());
        assertEquals(LocalTime.of(16, 0), createdOverride.getEndTime());
        assertEquals(ScheduleOverrideType.AVAILABLE, createdOverride.getType());
    }

    @Test
    void whenCreateScheduleOverrideFails_thenReturnEditViewWithErrorAndModel() throws Exception {
        User provider = createUserWithId(1L, "provider1", "123", "provider1@test.com", UserRole.PROVIDER);
        Activity ownActivity = createActivityWithId(400L, provider);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", provider);

        when(activityService.findActivityById(400L)).thenReturn(ownActivity);
        when(activityScheduleRuleService.getByActivity(400L)).thenReturn(List.of());
        when(activityScheduleOverrideService.getByActivity(400L)).thenReturn(List.of());
        when(activitySchedulePreviewService.buildRulePreviewDates(List.of())).thenReturn(Map.of());

        when(activityScheduleOverrideService.create(any(ActivityScheduleOverride.class)))
                .thenThrow(new IllegalArgumentException("Override intersects existing override"));

        mockMvc.perform(post("/provider/activities/400/schedule-overrides")
                        .session(session)
                        .param("date", "2026-04-21")
                        .param("startTime", "10:00")
                        .param("endTime", "11:00")
                        .param("type", "UNAVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(view().name("provider-activity-edit"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("activity"))
                .andExpect(model().attributeExists("scheduleRules"))
                .andExpect(model().attributeExists("scheduleOverrides"))
                .andExpect(model().attributeExists("rulePreviewDates"))
                .andExpect(model().attribute("scheduleOverrideError", "Override intersects existing override"));
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

    private Activity createActivityWithId(Long id, User provider) {
        Activity activity = new Activity();
        setId(activity, id);
        activity.setProvider(provider);
        activity.setTitle("Massage");
        activity.setDescription("Relax massage");
        activity.setPrice(new BigDecimal("100.00"));
        activity.setDurationMinutes(60);
        activity.setStatus(ActivityStatus.ACTIVE);
        return activity;
    }

    private void setId(Object target, Long id) {
        try {
            Field idField = target.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось установить id через reflection", e);
        }
    }
}