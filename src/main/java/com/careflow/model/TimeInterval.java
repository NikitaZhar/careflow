package com.careflow.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

public class TimeInterval {

    private final LocalTime start;
    private final LocalTime end;

    public TimeInterval(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public static TimeInterval of(LocalTime start, LocalTime end) {
        return new TimeInterval(start, end);
    }

    public boolean isValid() {
        return start != null
                && end != null
                && start.isBefore(end);
    }

    public boolean intersects(TimeInterval other) {
        Objects.requireNonNull(other, "other interval must not be null");

        if (!isValid() || !other.isValid()) {
            return false;
        }

        return start.isBefore(other.end) && end.isAfter(other.start);
    }

    public boolean contains(LocalTime time) {
        if (!isValid() || time == null) {
            return false;
        }

        return !time.isBefore(start) && time.isBefore(end);
    }

    public Duration duration() {
        if (!isValid()) {
            return Duration.ZERO;
        }

        return Duration.between(start, end);
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }
}