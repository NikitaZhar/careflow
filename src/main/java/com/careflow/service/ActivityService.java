package com.careflow.service;

import com.careflow.exception.ActivityNotFoundException;
import com.careflow.exception.UserNotFoundException;
import com.careflow.model.Activity;
import com.careflow.model.ActivityStatus;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.repository.ActivityRepository;
import com.careflow.repository.AppointmentRepository;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public ActivityService(ActivityRepository activityRepository,
                           AppointmentRepository appointmentRepository,
                           UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    public Activity saveActivity(Activity activity) {
        User provider = resolveProvider(activity);
        validateProviderRole(provider);

        activity.setProvider(provider);

        if (activity.getStatus() == null) {
            activity.setStatus(ActivityStatus.ACTIVE);
        }

        return activityRepository.save(activity);
    }

    public List<Activity> findAllActivities() {
        return activityRepository.findAll();
    }
    
    public List<Activity> findActivitiesByProviderId(Long providerId) {
        return activityRepository.findByProviderId(providerId);
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

        if (updatedActivity.getStatus() != null) {
            existingActivity.setStatus(updatedActivity.getStatus());
        }

        return activityRepository.save(existingActivity);
    }

    public void deleteActivityById(Long id) {
        Activity existingActivity = findActivityByIdOrThrow(id);

        if (appointmentRepository.existsByActivityId(id)) {
            throw new IllegalStateException("Activity cannot be deleted because it has appointments");
        }

        activityRepository.delete(existingActivity);
    }

    private Activity findActivityByIdOrThrow(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException(id));
    }

    private User resolveProvider(Activity activity) {
        if (activity.getProvider() == null || activity.getProvider().getId() == null) {
            throw new IllegalArgumentException("Provider id must not be null");
        }

        return userRepository.findById(activity.getProvider().getId())
                .orElseThrow(() -> new UserNotFoundException(activity.getProvider().getId()));
    }

    private void validateProviderRole(User provider) {
        if (provider.getRole() != UserRole.PROVIDER) {
            throw new IllegalArgumentException("Only PROVIDER can own activity");
        }
    }
}