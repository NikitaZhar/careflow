package com.careflow.controller;

import com.careflow.controller.auth.SessionUserValidator;
import com.careflow.model.Activity;
import com.careflow.model.Appointment;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.service.ActivityService;
import com.careflow.service.AppointmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
public class ProviderActivityController {

    private final SessionUserValidator sessionValidator;
    private final ActivityService activityService;
    private final AppointmentService appointmentService;

    public ProviderActivityController(SessionUserValidator sessionValidator,
                                      ActivityService activityService,
                                      AppointmentService appointmentService) {
        this.sessionValidator = sessionValidator;
        this.activityService = activityService;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/provider/activities")
    public String createActivity(@RequestParam String title,
                                 @RequestParam String description,
                                 @RequestParam BigDecimal price,
                                 @RequestParam Integer durationMinutes,
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

    @GetMapping("/provider/activities/{id}")
    public String showEditActivityPage(@PathVariable Long id,
                                       HttpSession session,
                                       Model model) {

        User user = sessionValidator.getValidUser(session, UserRole.PROVIDER);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Activity activity = activityService.findActivityById(id);
        if (!activity.getProvider().getId().equals(user.getId())) {
            return "redirect:/provider";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("activity", activity);
        model.addAttribute("availableAppointments",
                appointmentService.findAvailableAppointmentsByActivityId(activity.getId()));

        return "provider-activity-edit";
    }

    @PostMapping("/provider/activities/{id}")
    public String updateActivity(@PathVariable Long id,
                                 @RequestParam String title,
                                 @RequestParam String description,
                                 @RequestParam BigDecimal price,
                                 @RequestParam Integer durationMinutes,
                                 @RequestParam(required = false) List<String> newAppointmentDates,
                                 @RequestParam(required = false) List<String> newAppointmentHours,
                                 @RequestParam(required = false) List<String> newAppointmentMinutes,
                                 @RequestParam(required = false) List<Long> appointmentsToDelete,
                                 HttpSession session) {

        User user = sessionValidator.getValidUser(session, UserRole.PROVIDER);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Activity activity = activityService.findActivityById(id);
        if (!activity.getProvider().getId().equals(user.getId())) {
            return "redirect:/provider";
        }

        activity.setTitle(title);
        activity.setDescription(description);
        activity.setPrice(price);
        activity.setDurationMinutes(durationMinutes);
        activityService.saveActivity(activity);

        if (appointmentsToDelete != null) {
            for (Long appointmentId : appointmentsToDelete) {
                Appointment appointment = appointmentService.findAppointmentById(appointmentId);

                if (appointment.getActivity().getId().equals(activity.getId())) {
                    appointmentService.deleteAppointmentById(appointmentId);
                }
            }
        }

        if (newAppointmentDates != null && newAppointmentHours != null && newAppointmentMinutes != null) {
            for (int i = 0; i < newAppointmentDates.size(); i++) {
                LocalDate date = LocalDate.parse(newAppointmentDates.get(i));
                LocalTime time = LocalTime.parse(newAppointmentHours.get(i) + ":" + newAppointmentMinutes.get(i));
                LocalDateTime startTime = LocalDateTime.of(date, time);

                if (startTime.isBefore(LocalDateTime.now())) {
                    continue;
                }

                LocalDateTime endTime = startTime.plusMinutes(activity.getDurationMinutes());

                boolean exists = appointmentService.existsOverlappingAppointment(
                        activity.getId(),
                        startTime,
                        endTime
                );

                if (exists) {
                    continue;
                }

                Appointment appointment = new Appointment();
                appointment.setActivity(activity);
                appointment.setStartTime(startTime);
                appointment.setEndTime(endTime);

                appointmentService.saveAppointment(appointment);
            }
        }

        return "redirect:/provider";
    }

    @PostMapping("/provider/activities/{id}/delete")
    public String deleteActivity(@PathVariable Long id,
                                 HttpSession session) {

        User user = sessionValidator.getValidUser(session, UserRole.PROVIDER);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Activity activity = activityService.findActivityById(id);
        if (!activity.getProvider().getId().equals(user.getId())) {
            return "redirect:/provider";
        }

        activityService.deleteActivityById(id);
        return "redirect:/provider";
    }
}