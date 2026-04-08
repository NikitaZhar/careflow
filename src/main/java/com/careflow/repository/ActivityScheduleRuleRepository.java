package com.careflow.repository;

import com.careflow.model.ActivityScheduleRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface ActivityScheduleRuleRepository extends JpaRepository<ActivityScheduleRule, Long> {

    List<ActivityScheduleRule> findByActivityIdAndDayOfWeekAndActiveTrue(Long activityId, DayOfWeek dayOfWeek);

    List<ActivityScheduleRule> findByActivityId(Long activityId);
}