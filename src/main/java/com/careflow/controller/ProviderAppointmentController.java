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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
public class ProviderAppointmentController {

    private final SessionUserValidator sessionValidator;
    private final ActivityService activityService;
    private final AppointmentService appointmentService;

    public ProviderAppointmentController(SessionUserValidator sessionValidator,
                                         ActivityService activityService,
                                         AppointmentService appointmentService) {
        this.sessionValidator = sessionValidator;
        this.activityService = activityService;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/provider/activities/{id}/appointments")
    public String createAvailableAppointment(@PathVariable Long id,
                                             @RequestParam LocalDate date,
                                             @RequestParam String hour,
                                             @RequestParam String minute,
                                             HttpSession session) {

        User user = sessionValidator.getValidUser(session, UserRole.PROVIDER);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Activity activity = activityService.findActivityById(id);
        if (!activity.getProvider().getId().equals(user.getId())) {
            return "redirect:/provider";
        }

        LocalTime time = LocalTime.parse(hour + ":" + minute);
        LocalDateTime startTime = LocalDateTime.of(date, time);
        LocalDateTime endTime = startTime.plusMinutes(activity.getDurationMinutes());

        Appointment appointment = new Appointment();
        appointment.setActivity(activity);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);

        appointmentService.saveAppointment(appointment);

        return "redirect:/provider/activities/" + id;
    }

    @PostMapping("/provider/activities/{activityId}/appointments/{appointmentId}/delete")
    public String deleteAvailableAppointment(@PathVariable Long activityId,
                                             @PathVariable Long appointmentId,
                                             HttpSession session) {

        User user = sessionValidator.getValidUser(session, UserRole.PROVIDER);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Activity activity = activityService.findActivityById(activityId);
        if (!activity.getProvider().getId().equals(user.getId())) {
            return "redirect:/provider";
        }

        Appointment appointment = appointmentService.findAppointmentById(appointmentId);
        if (!appointment.getActivity().getId().equals(activity.getId())) {
            return "redirect:/provider/activities/" + activityId;
        }

        appointmentService.deleteAppointmentById(appointmentId);
        return "redirect:/provider/activities/" + activityId;
    }
}