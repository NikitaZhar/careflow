package com.careflow.controller;

import com.careflow.model.Appointment;
import com.careflow.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<Appointment> saveAppointment(@Valid @RequestBody Appointment appointment) {
        Appointment savedAppointment = appointmentService.saveAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAppointment);
    }

    @GetMapping
    public List<Appointment> findAllAppointments() {
        return appointmentService.findAllAppointments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> findAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.findAppointmentById(id));
    }

    @GetMapping("/provider/{providerId}")
    public List<Appointment> findAppointmentsByProviderId(@PathVariable Long providerId) {
        return appointmentService.findAppointmentsByProviderId(providerId);
    }

    @GetMapping("/client/{clientId}")
    public List<Appointment> findAppointmentsByClientId(@PathVariable Long clientId) {
        return appointmentService.findAppointmentsByClientId(clientId);
    }

    @GetMapping("/activity/{activityId}/available")
    public List<Appointment> findAvailableAppointmentsByActivityId(@PathVariable Long activityId) {
        return appointmentService.findAvailableAppointmentsByActivityId(activityId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Long id,
                                                         @Valid @RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, appointment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointmentById(@PathVariable Long id) {
        appointmentService.deleteAppointmentById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{appointmentId}/reserve")
    public ResponseEntity<Appointment> reserveAppointment(@PathVariable Long appointmentId,
                                                          @RequestParam Long clientId) {
        return ResponseEntity.ok(appointmentService.reserveAppointment(appointmentId, clientId));
    }

    @PatchMapping("/{appointmentId}/confirm")
    public ResponseEntity<Appointment> confirmAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.confirmAppointment(appointmentId));
    }

    @PatchMapping("/{appointmentId}/reject")
    public ResponseEntity<Appointment> rejectAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.rejectAppointment(appointmentId));
    }

    @PatchMapping("/{appointmentId}/pay")
    public ResponseEntity<Appointment> markAppointmentAsPaid(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.markAppointmentAsPaid(appointmentId));
    }

    @PatchMapping("/{appointmentId}/complete")
    public ResponseEntity<Appointment> completeAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.completeAppointment(appointmentId));
    }
}