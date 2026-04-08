package com.careflow.controller;

import com.careflow.controller.auth.SessionUserValidator;
import com.careflow.model.Activity;
import com.careflow.model.ActivityScheduleOverride;
import com.careflow.model.ActivityScheduleRule;
import com.careflow.model.ScheduleOverrideType;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.service.ActivityScheduleOverrideService;
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
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProviderActivityController {

    private final SessionUserValidator sessionValidator;
    private final ActivityService activityService;
    private final ActivityScheduleRuleService activityScheduleRuleService;
    private final ActivityScheduleOverrideService activityScheduleOverrideService;

    public ProviderActivityController(SessionUserValidator sessionValidator,
                                      ActivityService activityService,
                                      ActivityScheduleRuleService activityScheduleRuleService,
                                      ActivityScheduleOverrideService activityScheduleOverrideService) {
        this.sessionValidator = sessionValidator;
        this.activityService = activityService;
        this.activityScheduleRuleService = activityScheduleRuleService;
        this.activityScheduleOverrideService = activityScheduleOverrideService;
    }

    @PostMapping("/provider/activities")
    public String createActivity(@RequestParam String title,
                                 @RequestParam String description,
                                 @RequestParam BigDecimal price,
                                 @RequestParam Integer durationMinutes,
                                 HttpSession session) {

        User provider = getValidProvider(session);
        if (provider == null) {
            return "redirect:/auth/login";
        }

        Activity activity = new Activity();
        activity.setProvider(provider);
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setPrice(price);
        activity.setDurationMinutes(durationMinutes);

        activityService.saveActivity(activity);
        return "redirect:/provider";
    }

    @GetMapping("/provider/activities/{id}")
    public String showEditActivityPage(@PathVariable Long id,
                                       HttpSession session,
                                       Model model) {

        User provider = getValidProvider(session);
        if (provider == null) {
            return "redirect:/auth/login";
        }

        Activity activity = getOwnedActivityOrNull(id, provider);
        if (activity == null) {
            return "redirect:/provider";
        }

        populateEditModel(model, provider, activity);
        return "provider-activity-edit";
    }

    @PostMapping("/provider/activities/{id}")
    public String updateActivity(@PathVariable Long id,
                                 @RequestParam String title,
                                 @RequestParam String description,
                                 @RequestParam BigDecimal price,
                                 @RequestParam Integer durationMinutes,
                                 HttpSession session) {

        User provider = getValidProvider(session);
        if (provider == null) {
            return "redirect:/auth/login";
        }

        Activity activity = getOwnedActivityOrNull(id, provider);
        if (activity == null) {
            return "redirect:/provider";
        }

        activity.setTitle(title);
        activity.setDescription(description);
        activity.setPrice(price);
        activity.setDurationMinutes(durationMinutes);

        activityService.saveActivity(activity);
        return "redirect:/provider/activities/" + activity.getId();
    }

    @PostMapping("/provider/activities/{id}/delete")
    public String deleteActivity(@PathVariable Long id,
                                 HttpSession session) {

        User provider = getValidProvider(session);
        if (provider == null) {
            return "redirect:/auth/login";
        }

        Activity activity = getOwnedActivityOrNull(id, provider);
        if (activity == null) {
            return "redirect:/provider";
        }

        activityService.deleteActivityById(activity.getId());
        return "redirect:/provider";
    }

    @PostMapping("/provider/activities/{id}/schedule-rules")
    public String createScheduleRule(@PathVariable Long id,
                                     @RequestParam DayOfWeek dayOfWeek,
                                     @RequestParam LocalTime startTime,
                                     @RequestParam LocalTime endTime,
                                     HttpSession session) {

        User provider = getValidProvider(session);
        if (provider == null) {
            return "redirect:/auth/login";
        }

        Activity activity = getOwnedActivityOrNull(id, provider);
        if (activity == null) {
            return "redirect:/provider";
        }

        ActivityScheduleRule rule = new ActivityScheduleRule();
        rule.setActivity(activity);
        rule.setDayOfWeek(dayOfWeek);
        rule.setStartTime(startTime);
        rule.setEndTime(endTime);
        rule.setActive(true);

        activityScheduleRuleService.create(rule);

        return "redirect:/provider/activities/" + activity.getId();
    }

    @PostMapping("/provider/activities/{activityId}/schedule-rules/{ruleId}/delete")
    public String deleteScheduleRule(@PathVariable Long activityId,
                                     @PathVariable Long ruleId,
                                     HttpSession session) {

        User provider = getValidProvider(session);
        if (provider == null) {
            return "redirect:/auth/login";
        }

        Activity activity = getOwnedActivityOrNull(activityId, provider);
        if (activity == null) {
            return "redirect:/provider";
        }

        ActivityScheduleRule rule = activityScheduleRuleService.getById(ruleId);
        if (rule == null) {
            return "redirect:/provider/activities/" + activity.getId();
        }

        if (!rule.getActivity().getId().equals(activity.getId())) {
            return "redirect:/provider";
        }

        activityScheduleRuleService.delete(ruleId);

        return "redirect:/provider/activities/" + activity.getId();
    }

    @PostMapping("/provider/activities/{id}/schedule-overrides")
    public String createScheduleOverride(@PathVariable Long id,
                                         @RequestParam LocalDate date,
                                         @RequestParam LocalTime startTime,
                                         @RequestParam LocalTime endTime,
                                         @RequestParam ScheduleOverrideType type,
                                         HttpSession session,
                                         Model model) {

        User provider = getValidProvider(session);
        if (provider == null) {
            return "redirect:/auth/login";
        }

        Activity activity = getOwnedActivityOrNull(id, provider);
        if (activity == null) {
            return "redirect:/provider";
        }

        ActivityScheduleOverride scheduleOverride = new ActivityScheduleOverride();
        scheduleOverride.setActivity(activity);
        scheduleOverride.setDate(date);
        scheduleOverride.setStartTime(startTime);
        scheduleOverride.setEndTime(endTime);
        scheduleOverride.setType(type);

        try {
            activityScheduleOverrideService.create(scheduleOverride);
            return "redirect:/provider/activities/" + activity.getId();
        } catch (IllegalArgumentException exception) {
            populateEditModel(model, provider, activity);
            model.addAttribute("scheduleOverrideError", exception.getMessage());
            return "provider-activity-edit";
        }
    }

    @PostMapping("/provider/activities/{activityId}/schedule-overrides/{overrideId}/delete")
    public String deleteScheduleOverride(@PathVariable Long activityId,
                                         @PathVariable Long overrideId,
                                         HttpSession session) {

        User provider = getValidProvider(session);
        if (provider == null) {
            return "redirect:/auth/login";
        }

        Activity activity = getOwnedActivityOrNull(activityId, provider);
        if (activity == null) {
            return "redirect:/provider";
        }

        ActivityScheduleOverride scheduleOverride = activityScheduleOverrideService.getById(overrideId);
        if (scheduleOverride == null) {
            return "redirect:/provider/activities/" + activity.getId();
        }

        if (!scheduleOverride.getActivity().getId().equals(activity.getId())) {
            return "redirect:/provider";
        }

        activityScheduleOverrideService.delete(overrideId);

        return "redirect:/provider/activities/" + activity.getId();
    }

    private User getValidProvider(HttpSession session) {
        return sessionValidator.getValidUser(session, UserRole.PROVIDER);
    }

    private Activity getOwnedActivityOrNull(Long activityId, User provider) {
        Activity activity = activityService.findActivityById(activityId);

        if (activity == null) {
            return null;
        }

        if (activity.getProvider() == null) {
            return null;
        }

        if (!activity.getProvider().getId().equals(provider.getId())) {
            return null;
        }

        return activity;
    }

    private void populateEditModel(Model model, User provider, Activity activity) {
        model.addAttribute("currentUser", provider);
        model.addAttribute("activity", activity);

        List<ActivityScheduleRule> scheduleRules =
                activityScheduleRuleService.getByActivity(activity.getId());

        model.addAttribute("scheduleRules", scheduleRules);
        model.addAttribute("scheduleOverrides",
                activityScheduleOverrideService.getByActivity(activity.getId()));
        model.addAttribute("rulePreviewDates", buildRulePreviewDates(scheduleRules));
    }
    
    private Map<Long, String> buildRulePreviewDates(List<ActivityScheduleRule> rules) {
        Map<Long, String> previewDates = new LinkedHashMap<>();

        for (ActivityScheduleRule rule : rules) {
            previewDates.put(rule.getId(), buildNextDatesText(rule.getDayOfWeek(), 3));
        }

        return previewDates;
    }

    private String buildNextDatesText(java.time.DayOfWeek dayOfWeek, int count) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        StringBuilder result = new StringBuilder();

        LocalDate currentDate = today;
        int found = 0;

        while (found < count) {
            if (currentDate.getDayOfWeek().equals(dayOfWeek)) {
                if (!result.isEmpty()) {
                    result.append(", ");
                }
                result.append(currentDate.format(formatter));
                found++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return result.toString();
    }
}