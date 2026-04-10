package com.careflow.model;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Entity
@Table(name = "activity_schedule_rules")
public class ActivityScheduleRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Activity activity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private java.time.LocalTime startTime;

    @Column(nullable = false)
    private java.time.LocalTime endTime;

    @Column(nullable = false)
    private boolean active = true;

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

    public boolean isOnDay(DayOfWeek otherDayOfWeek) {
        return dayOfWeek != null && dayOfWeek.equals(otherDayOfWeek);
    }

    public boolean appliesTo(LocalDate date) {
        return date != null
                && dayOfWeek != null
                && dayOfWeek.equals(date.getDayOfWeek());
    }

    public boolean overlaps(ActivityScheduleRule other) {
        if (other == null) {
            return false;
        }

        if (!isOnDay(other.getDayOfWeek())) {
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

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public java.time.LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(java.time.LocalTime startTime) {
        this.startTime = startTime;
    }

    public java.time.LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(java.time.LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}