package com.careflow.model;

import java.time.LocalTime;

public class TimeInterval {

    private LocalTime start;
    private LocalTime end;

    public TimeInterval(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public boolean intersects(TimeInterval other) {
        return start.isBefore(other.end) && end.isAfter(other.start);
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }
}