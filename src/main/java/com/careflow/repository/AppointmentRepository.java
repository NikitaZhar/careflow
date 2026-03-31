package com.careflow.repository;

import com.careflow.model.Appointment;
import com.careflow.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByActivityProviderId(Long providerId);

    List<Appointment> findByClientId(Long clientId);

    List<Appointment> findByActivityIdAndStatus(Long activityId, AppointmentStatus status);

    boolean existsByActivityId(Long activityId);

    boolean existsByClientId(Long clientId);

    boolean existsByActivityIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long activityId,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    boolean existsByActivityIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long activityId,
            Long id,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
}