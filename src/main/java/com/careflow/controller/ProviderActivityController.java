package com.careflow.controller;

import com.careflow.controller.auth.SessionUserValidator;
import com.careflow.model.Activity;
import com.careflow.model.ActivityScheduleOverride;
import com.careflow.model.ActivityScheduleRule;
import com.careflow.model.ScheduleOverrideType;
import com.careflow.model.User;
import com.careflow.service.ActivityScheduleOverrideService;
import com.careflow.service.ActivitySchedulePreviewService;
import com.careflow.service.ActivityScheduleRuleService;
import com.careflow.service.ActivityService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
public class ProviderActivityController {

    private static final String LOGIN_REDIRECT = "redirect:/auth/login";
    private static final String PROVIDER_REDIRECT = "redirect:/provider";
    private static final String PROVIDER_ACTIVITY_EDIT_VIEW = "provider-activity-edit";

    private final SessionUserValidator sessionValidator;
    private final ActivityService activityService;
    private final ActivityScheduleRuleService activityScheduleRuleService;
    private final ActivityScheduleOverrideService activityScheduleOverrideService;
    private final ActivitySchedulePreviewService activitySchedulePreviewService;

    public ProviderActivityController(SessionUserValidator sessionValidator,
                                      ActivityService activityService,
                                      ActivityScheduleRuleService activityScheduleRuleService,
                                      ActivityScheduleOverrideService activityScheduleOverrideService,
                                      ActivitySchedulePreviewService activitySchedulePreviewService) {
        this.sessionValidator = sessionValidator;
        this.activityService = activityService;
        this.activityScheduleRuleService = activityScheduleRuleService;
        this.activityScheduleOverrideService = activityScheduleOverrideService;
        this.activitySchedulePreviewService = activitySchedulePreviewService;
    }

    @PostMapping("/provider/activities")
    public String createActivity(@RequestParam String title,
                                 @RequestParam String description,
                                 @RequestParam BigDecimal price,
                                 @RequestParam Integer durationMinutes,
                                 HttpSession session) {

        User provider = getValidProviderOrNull(session);
        if (provider == null) {
            return LOGIN_REDIRECT;
        }

        Activity activity = new Activity();
        activity.setProvider(provider);
        activity.updateDetails(title, description, price, durationMinutes);

        activityService.saveActivity(activity);
        return PROVIDER_REDIRECT;
    }

    @GetMapping("/provider/activities/{id}")
    public String showEditActivityPage(@PathVariable Long id,
                                       HttpSession session,
                                       Model model) {

        User provider = getValidProviderOrNull(session);
        if (provider == null) {
            return LOGIN_REDIRECT;
        }

        Activity activity = getOwnedActivityOrNull(id, provider);
        if (activity == null) {
            return PROVIDER_REDIRECT;
        }

        populateEditModel(model, provider, activity);
        return PROVIDER_ACTIVITY_EDIT_VIEW;
    }

    @PostMapping("/provider/activities/{id}")
    public String updateActivity(@PathVariable Long id,
                                 @RequestParam String title,
                                 @RequestParam String description,
                                 @RequestParam BigDecimal price,
                                 @RequestParam Integer durationMinutes,
                                 HttpSession session) {

        User provider = getValidProviderOrNull(session);
        if (provider == null) {
            return LOGIN_REDIRECT;
        }

        Activity activity = getOwnedActivityOrNull(id, provider);
        if (activity == null) {
            return PROVIDER_REDIRECT;
        }

        activity.updateDetails(title, description, price, durationMinutes);
        activityService.saveActivity(activity);

        return redirectToActivity(activity.getId());
    }

    @PostMapping("/provider/activities/{id}/delete")
    public String deleteActivity(@PathVariable Long id,
                                 HttpSession session) {

        User provider = getValidProviderOrNull(session);
        if (provider == null) {
            return LOGIN_REDIRECT;
        }

        Activity activity = getOwnedActivityOrNull(id, provider);
        if (activity == null) {
            return PROVIDER_REDIRECT;
        }

        activityService.deleteActivityById(activity.getId());
        return PROVIDER_REDIRECT;
    }

    @PostMapping("/provider/activities/{id}/schedule-rules")
    public String createScheduleRule(@PathVariable Long id,
                                     @RequestParam DayOfWeek dayOfWeek,
                                     @RequestParam LocalTime startTime,
                                     @RequestParam LocalTime endTime,
                                     HttpSession session) {

        User provider = getValidProviderOrNull(session);
        if (provider == null) {
            return LOGIN_REDIRECT;
        }

        Activity activity = getOwnedActivityOrNull(id, provider);
        if (activity == null) {
            return PROVIDER_REDIRECT;
        }

        ActivityScheduleRule rule = new ActivityScheduleRule();
        rule.setActivity(activity);
        rule.setDayOfWeek(dayOfWeek);
        rule.setStartTime(startTime);
        rule.setEndTime(endTime);
        rule.setActive(true);

        activityScheduleRuleService.create(rule);
        return redirectToActivity(activity.getId());
    }

    @PostMapping("/provider/activities/{activityId}/schedule-rules/{ruleId}/delete")
    public String deleteScheduleRule(@PathVariable Long activityId,
                                     @PathVariable Long ruleId,
                                     HttpSession session) {

        User provider = getValidProviderOrNull(session);
        if (provider == null) {
            return LOGIN_REDIRECT;
        }

        Activity activity = getOwnedActivityOrNull(activityId, provider);
        if (activity == null) {
            return PROVIDER_REDIRECT;
        }

        ActivityScheduleRule rule = activityScheduleRuleService.getById(ruleId);
        if (rule == null) {
            return redirectToActivity(activity.getId());
        }

        if (rule.getActivity() == null || !rule.getActivity().getId().equals(activity.getId())) {
            return PROVIDER_REDIRECT;
        }

        activityScheduleRuleService.delete(ruleId);
        return redirectToActivity(activity.getId());
    }

    @PostMapping("/provider/activities/{id}/schedule-overrides")
    public String createScheduleOverride(@PathVariable Long id,
                                         @RequestParam LocalDate date,
                                         @RequestParam LocalTime startTime,
                                         @RequestParam LocalTime endTime,
                                         @RequestParam ScheduleOverrideType type,
                                         HttpSession session,
                                         Model model) {

        User provider = getValidProviderOrNull(session);
        if (provider == null) {
            return LOGIN_REDIRECT;
        }

        Activity activity = getOwnedActivityOrNull(id, provider);
        if (activity == null) {
            return PROVIDER_REDIRECT;
        }

        ActivityScheduleOverride scheduleOverride = new ActivityScheduleOverride();
        scheduleOverride.setActivity(activity);
        scheduleOverride.setDate(date);
        scheduleOverride.setStartTime(startTime);
        scheduleOverride.setEndTime(endTime);
        scheduleOverride.setType(type);

        try {
            activityScheduleOverrideService.create(scheduleOverride);
            return redirectToActivity(activity.getId());
        } catch (IllegalArgumentException exception) {
            populateEditModel(model, provider, activity);
            model.addAttribute("scheduleOverrideError", exception.getMessage());
            return PROVIDER_ACTIVITY_EDIT_VIEW;
        }
    }

    @PostMapping("/provider/activities/{activityId}/schedule-overrides/{overrideId}/delete")
    public String deleteScheduleOverride(@PathVariable Long activityId,
                                         @PathVariable Long overrideId,
                                         HttpSession session) {

        User provider = getValidProviderOrNull(session);
        if (provider == null) {
            return LOGIN_REDIRECT;
        }

        Activity activity = getOwnedActivityOrNull(activityId, provider);
        if (activity == null) {
            return PROVIDER_REDIRECT;
        }

        ActivityScheduleOverride scheduleOverride = activityScheduleOverrideService.getById(overrideId);
        if (scheduleOverride == null) {
            return redirectToActivity(activity.getId());
        }

        if (scheduleOverride.getActivity() == null
                || !scheduleOverride.getActivity().getId().equals(activity.getId())) {
            return PROVIDER_REDIRECT;
        }

        activityScheduleOverrideService.delete(overrideId);
        return redirectToActivity(activity.getId());
    }

    private User getValidProviderOrNull(HttpSession session) {
        return sessionValidator.getValidProvider(session);
    }

    private Activity getOwnedActivityOrNull(Long activityId, User provider) {
        Activity activity = activityService.findActivityById(activityId);

        if (activity == null) {
            return null;
        }

        return activity.belongsTo(provider) ? activity : null;
    }

    private void populateEditModel(Model model, User provider, Activity activity) {
        model.addAttribute("currentUser", provider);
        model.addAttribute("activity", activity);

        List<ActivityScheduleRule> scheduleRules =
                activityScheduleRuleService.getByActivity(activity.getId());

        model.addAttribute("scheduleRules", scheduleRules);
        model.addAttribute("scheduleOverrides",
                activityScheduleOverrideService.getByActivity(activity.getId()));
        model.addAttribute("rulePreviewDates",
                activitySchedulePreviewService.buildRulePreviewDates(scheduleRules));
    }

    private String redirectToActivity(Long activityId) {
        return "redirect:/provider/activities/" + activityId;
    }
}