package com.careflow.repository;

import com.careflow.model.ActivityScheduleOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ActivityScheduleOverrideRepository extends JpaRepository<ActivityScheduleOverride, Long> {

    List<ActivityScheduleOverride> findByActivityIdAndDate(Long activityId, LocalDate date);

    List<ActivityScheduleOverride> findByActivityIdOrderByDateAscStartTimeAsc(Long activityId);

    boolean existsByActivityIdAndDateAndStartTimeLessThanAndEndTimeGreaterThan(
            Long activityId,
            LocalDate date,
            LocalTime end,
            LocalTime start
    );
}