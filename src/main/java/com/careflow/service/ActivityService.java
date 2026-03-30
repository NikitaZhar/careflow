package com.careflow.service;

import com.careflow.exception.ActivityNotFoundException;
import com.careflow.model.Activity;
import com.careflow.repository.ActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public Activity saveActivity(Activity activity) {
        return activityRepository.save(activity);
    }

    public List<Activity> findAllActivities() {
        return activityRepository.findAll();
    }

    public Activity findActivityById(Long id) {
        return findActivityByIdOrThrow(id);
    }

    public Activity updateActivity(Long id, Activity updatedActivity) {
        Activity existingActivity = findActivityByIdOrThrow(id);

        existingActivity.setTitle(updatedActivity.getTitle());
        existingActivity.setDescription(updatedActivity.getDescription());
        existingActivity.setPrice(updatedActivity.getPrice());
        existingActivity.setDurationMinutes(updatedActivity.getDurationMinutes());
        existingActivity.setStatus(updatedActivity.getStatus());

        return activityRepository.save(existingActivity);
    }

    public void deleteActivityById(Long id) {
        Activity existingActivity = findActivityByIdOrThrow(id);
        activityRepository.delete(existingActivity);
    }

    private Activity findActivityByIdOrThrow(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException(id));
    }
}