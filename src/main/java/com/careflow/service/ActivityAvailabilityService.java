package com.careflow.service;

import com.careflow.model.Activity;
import com.careflow.model.ScheduleOverrideType;
import com.careflow.model.TimeInterval;
import com.careflow.repository.ActivityScheduleOverrideRepository;
import com.careflow.repository.ActivityScheduleRuleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ActivityAvailabilityService {

    private final ActivityScheduleRuleRepository ruleRepository;
    private final ActivityScheduleOverrideRepository overrideRepository;

    public ActivityAvailabilityService(ActivityScheduleRuleRepository ruleRepository,
                                       ActivityScheduleOverrideRepository overrideRepository) {
        this.ruleRepository = ruleRepository;
        this.overrideRepository = overrideRepository;
    }

    public List<TimeInterval> getAvailability(Activity activity, LocalDate date) {
        List<TimeInterval> result = new ArrayList<>();

        var rules = ruleRepository.findByActivityIdAndDayOfWeekAndActiveTrue(
                activity.getId(),
                date.getDayOfWeek()
        );

        for (var rule : rules) {
            result.add(new TimeInterval(rule.getStartTime(), rule.getEndTime()));
        }

        var overrides = overrideRepository.findByActivityIdAndDate(activity.getId(), date);

        for (var override : overrides) {
            TimeInterval overrideInterval = new TimeInterval(
                    override.getStartTime(),
                    override.getEndTime()
            );

            if (override.getType() == ScheduleOverrideType.UNAVAILABLE) {
                result = subtract(result, overrideInterval);
            } else {
                result.add(overrideInterval);
            }
        }

        return result;
    }

    private List<TimeInterval> subtract(List<TimeInterval> base, TimeInterval blocked) {
        List<TimeInterval> result = new ArrayList<>();

        for (TimeInterval interval : base) {
            if (!interval.intersects(blocked)) {
                result.add(interval);
                continue;
            }

            if (interval.getStart().isBefore(blocked.getStart())) {
                result.add(new TimeInterval(interval.getStart(), blocked.getStart()));
            }

            if (interval.getEnd().isAfter(blocked.getEnd())) {
                result.add(new TimeInterval(blocked.getEnd(), interval.getEnd()));
            }
        }

        return result;
    }
}