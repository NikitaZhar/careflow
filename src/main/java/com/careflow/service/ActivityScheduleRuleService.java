package com.careflow.service;

import com.careflow.model.ActivityScheduleRule;
import com.careflow.repository.ActivityScheduleRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityScheduleRuleService {

    private final ActivityScheduleRuleRepository repository;

    public ActivityScheduleRuleService(ActivityScheduleRuleRepository repository) {
        this.repository = repository;
    }

    public ActivityScheduleRule create(ActivityScheduleRule rule) {
        return repository.save(rule);
    }

    public ActivityScheduleRule getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<ActivityScheduleRule> getByActivity(Long activityId) {
        return repository.findByActivityId(activityId);
    }
}