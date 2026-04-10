package com.careflow.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "activity_schedule_overrides")
public class ActivityScheduleOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Activity activity;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleOverrideType type;

    @PrePersist
    @PreUpdate
    private void validate() {
        if (!hasValidTimeRange()) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    public TimeInterval toInterval() {
        return TimeInterval.of(startTime, endTime);
    }

    public boolean hasValidTimeRange() {
        return toInterval().isValid();
    }

    public boolean isOnDate(LocalDate otherDate) {
        return date != null && date.equals(otherDate);
    }

    public boolean overlaps(ActivityScheduleOverride other) {
        if (other == null) {
            return false;
        }

        if (!isOnDate(other.getDate())) {
            return false;
        }

        return toInterval().intersects(other.toInterval());
    }

    // ===== GETTERS / SETTERS =====

    public Long getId() {
        return id;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public ScheduleOverrideType getType() {
        return type;
    }

    public void setType(ScheduleOverrideType type) {
        this.type = type;
    }
}