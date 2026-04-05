package com.careflow.controller;

import com.careflow.controller.auth.SessionUserValidator;
import com.careflow.model.Activity;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.service.ActivityService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Controller
public class ProviderController {

    private final SessionUserValidator sessionValidator;
    private final ActivityService activityService;

    public ProviderController(SessionUserValidator sessionValidator,
                              ActivityService activityService) {
        this.sessionValidator = sessionValidator;
        this.activityService = activityService;
    }

    @GetMapping("/provider")
    public String showProviderPage(HttpSession session, Model model) {
        User user = sessionValidator.getValidUser(session, UserRole.PROVIDER);
        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("activities", activityService.findActivitiesByProviderId(user.getId()));
        return "provider";
    }

    @PostMapping("/provider/activities")
    public String createActivity(@RequestParam String title,
                                 @RequestParam String description,
                                 @RequestParam BigDecimal price,
                                 @RequestParam Integer durationMinutes,
                                 @RequestParam(required = false) MultipartFile document,
                                 @RequestParam(required = false) MultipartFile video,
                                 HttpSession session) {

        User user = sessionValidator.getValidUser(session, UserRole.PROVIDER);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Activity activity = new Activity();
        activity.setProvider(user);
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setPrice(price);
        activity.setDurationMinutes(durationMinutes);

        activityService.saveActivity(activity);

        return "redirect:/provider";
    }
}