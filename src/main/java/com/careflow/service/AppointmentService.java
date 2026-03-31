package com.careflow.service;

import com.careflow.exception.AppointmentNotFoundException;
import com.careflow.exception.UserNotFoundException;
import com.careflow.model.Activity;
import com.careflow.model.ActivityStatus;
import com.careflow.model.Appointment;
import com.careflow.model.AppointmentStatus;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.repository.ActivityRepository;
import com.careflow.repository.AppointmentRepository;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              ActivityRepository activityRepository,
                              UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    public Appointment saveAppointment(Appointment appointment) {
        validateAppointmentTime(appointment.getStartTime(), appointment.getEndTime());

        Activity activity = resolveActivity(appointment);
        validateActivityIsActive(activity);
        validateProviderRole(activity.getProvider());
        validateNoTimeOverlapForCreate(activity.getId(), appointment.getStartTime(), appointment.getEndTime());

        appointment.setActivity(activity);
        appointment.setClient(null);
        appointment.setStatus(AppointmentStatus.AVAILABLE);

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> findAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Appointment findAppointmentById(Long id) {
        return findAppointmentByIdOrThrow(id);
    }

    public List<Appointment> findAppointmentsByProviderId(Long providerId) {
        return appointmentRepository.findByActivityProviderId(providerId);
    }

    public List<Appointment> findAppointmentsByClientId(Long clientId) {
        return appointmentRepository.findByClientId(clientId);
    }

    public List<Appointment> findAvailableAppointmentsByActivityId(Long activityId) {
        return appointmentRepository.findByActivityIdAndStatus(activityId, AppointmentStatus.AVAILABLE);
    }

    public Appointment updateAppointment(Long id, Appointment updatedAppointment) {
        Appointment existingAppointment = findAppointmentByIdOrThrow(id);

        if (existingAppointment.getStatus() != AppointmentStatus.AVAILABLE) {
            throw new IllegalStateException("Only AVAILABLE appointment can be edited");
        }

        validateAppointmentTime(updatedAppointment.getStartTime(), updatedAppointment.getEndTime());

        Activity activity = resolveActivity(updatedAppointment);
        validateActivityIsActive(activity);
        validateProviderRole(activity.getProvider());
        validateNoTimeOverlapForUpdate(
                activity.getId(),
                existingAppointment.getId(),
                updatedAppointment.getStartTime(),
                updatedAppointment.getEndTime()
        );

        existingAppointment.setActivity(activity);
        existingAppointment.setStartTime(updatedAppointment.getStartTime());
        existingAppointment.setEndTime(updatedAppointment.getEndTime());

        return appointmentRepository.save(existingAppointment);
    }

    public void deleteAppointmentById(Long id) {
        Appointment existingAppointment = findAppointmentByIdOrThrow(id);

        if (existingAppointment.getStatus() != AppointmentStatus.AVAILABLE) {
            throw new IllegalStateException("Only AVAILABLE appointment can be deleted");
        }

        appointmentRepository.delete(existingAppointment);
    }

    public Appointment reserveAppointment(Long appointmentId, Long clientId) {
        Appointment appointment = findAppointmentByIdOrThrow(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.AVAILABLE) {
            throw new IllegalStateException("Only AVAILABLE appointment can be reserved");
        }

        User client = findUserByIdOrThrow(clientId);
        validateClientRole(client);
        validateClientIsNotProvider(client, appointment.getActivity().getProvider());

        appointment.setClient(client);
        appointment.setStatus(AppointmentStatus.PENDING);

        return appointmentRepository.save(appointment);
    }

    public Appointment confirmAppointment(Long appointmentId) {
        Appointment appointment = findAppointmentByIdOrThrow(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING appointment can be confirmed");
        }

        appointment.setStatus(AppointmentStatus.AWAITING_PAYMENT);
        return appointmentRepository.save(appointment);
    }

    public Appointment rejectAppointment(Long appointmentId) {
        Appointment appointment = findAppointmentByIdOrThrow(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING appointment can be rejected");
        }

        appointment.setStatus(AppointmentStatus.REJECTED);
        return appointmentRepository.save(appointment);
    }

    public Appointment markAppointmentAsPaid(Long appointmentId) {
        Appointment appointment = findAppointmentByIdOrThrow(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException("Only AWAITING_PAYMENT appointment can be marked as paid");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentRepository.save(appointment);
    }

    public Appointment completeAppointment(Long appointmentId) {
        Appointment appointment = findAppointmentByIdOrThrow(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED appointment can be completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appointment);
    }

    private Appointment findAppointmentByIdOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    private Activity resolveActivity(Appointment appointment) {
        if (appointment.getActivity() == null || appointment.getActivity().getId() == null) {
            throw new IllegalArgumentException("Activity id must not be null");
        }

        return activityRepository.findById(appointment.getActivity().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Activity with id " + appointment.getActivity().getId() + " not found"
                ));
    }

    private User findUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private void validateAppointmentTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time must not be null");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    private void validateProviderRole(User provider) {
        if (provider.getRole() != UserRole.PROVIDER) {
            throw new IllegalArgumentException("Activity owner must have PROVIDER role");
        }
    }

    private void validateClientRole(User client) {
        if (client.getRole() != UserRole.CLIENT) {
            throw new IllegalArgumentException("Only CLIENT can reserve appointment");
        }
    }

    private void validateClientIsNotProvider(User client, User provider) {
        if (client.getId().equals(provider.getId())) {
            throw new IllegalArgumentException("Provider cannot reserve own appointment");
        }
    }

    private void validateActivityIsActive(Activity activity) {
        if (activity.getStatus() != ActivityStatus.ACTIVE) {
            throw new IllegalStateException("Appointments can be created only for ACTIVE activities");
        }
    }

    private void validateNoTimeOverlapForCreate(Long activityId, LocalDateTime startTime, LocalDateTime endTime) {
        if (appointmentRepository.existsByActivityIdAndStartTimeLessThanAndEndTimeGreaterThan(
                activityId, endTime, startTime)) {
            throw new IllegalArgumentException("Appointment time overlaps with an existing appointment");
        }
    }

    private void validateNoTimeOverlapForUpdate(Long activityId,
                                                Long appointmentId,
                                                LocalDateTime startTime,
                                                LocalDateTime endTime) {
        if (appointmentRepository.existsByActivityIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
                activityId, appointmentId, endTime, startTime)) {
            throw new IllegalArgumentException("Appointment time overlaps with an existing appointment");
        }
    }
}