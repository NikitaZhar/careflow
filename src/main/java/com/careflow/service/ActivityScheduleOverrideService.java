package com.careflow.service;

import com.careflow.model.ActivityScheduleOverride;
import com.careflow.repository.ActivityScheduleOverrideRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ActivityScheduleOverrideService {

    private final ActivityScheduleOverrideRepository repository;

    public ActivityScheduleOverrideService(ActivityScheduleOverrideRepository repository) {
        this.repository = repository;
    }

    public ActivityScheduleOverride create(ActivityScheduleOverride override) {

        boolean exists = repository.existsByActivityIdAndDateAndStartTimeLessThanAndEndTimeGreaterThan(
                override.getActivity().getId(),
                override.getDate(),
                override.getEndTime(),
                override.getStartTime()
        );

        if (exists) {
            throw new IllegalArgumentException("Override intersects existing override");
        }

        return repository.save(override);
    }

    public ActivityScheduleOverride getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<ActivityScheduleOverride> getByActivity(Long activityId) {
        return repository.findByActivityIdOrderByDateAscStartTimeAsc(activityId);
    }

    public List<ActivityScheduleOverride> getByActivityAndDate(Long activityId, LocalDate date) {
        return repository.findByActivityIdAndDate(activityId, date);
    }
}