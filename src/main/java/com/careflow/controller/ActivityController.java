package com.careflow.controller;

import com.careflow.model.Activity;
import com.careflow.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<Activity> saveActivity(@Valid @RequestBody Activity activity) {
        Activity savedActivity = activityService.saveActivity(activity);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedActivity);
    }

    @GetMapping
    public List<Activity> findAllActivities() {
        return activityService.findAllActivities();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Activity> findActivityById(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.findActivityById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Activity> updateActivity(@PathVariable Long id,
                                                   @Valid @RequestBody Activity activity) {
        return ResponseEntity.ok(activityService.updateActivity(id, activity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivityById(@PathVariable Long id) {
        activityService.deleteActivityById(id);
        return ResponseEntity.noContent().build();
    }
}