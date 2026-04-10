package com.careflow;

import com.careflow.model.ActivityScheduleOverride;
import com.careflow.model.ScheduleOverrideType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ActivityScheduleOverrideTest {

    @Test
    void hasValidTimeRangeReturnsTrueForValidInterval() {
        ActivityScheduleOverride override = new ActivityScheduleOverride();
        override.setDate(LocalDate.of(2026, 4, 15));
        override.setStartTime(LocalTime.of(10, 0));
        override.setEndTime(LocalTime.of(11, 0));
        override.setType(ScheduleOverrideType.AVAILABLE);

        assertTrue(override.hasValidTimeRange());
    }

    @Test
    void hasValidTimeRangeReturnsFalseForInvalidInterval() {
        ActivityScheduleOverride override = new ActivityScheduleOverride();
        override.setDate(LocalDate.of(2026, 4, 15));
        override.setStartTime(LocalTime.of(11, 0));
        override.setEndTime(LocalTime.of(10, 0));
        override.setType(ScheduleOverrideType.AVAILABLE);

        assertFalse(override.hasValidTimeRange());
    }

    @Test
    void isOnDateReturnsTrueForSameDate() {
        ActivityScheduleOverride override = new ActivityScheduleOverride();
        override.setDate(LocalDate.of(2026, 4, 15));
        override.setStartTime(LocalTime.of(10, 0));
        override.setEndTime(LocalTime.of(11, 0));
        override.setType(ScheduleOverrideType.AVAILABLE);

        assertTrue(override.isOnDate(LocalDate.of(2026, 4, 15)));
    }

    @Test
    void overlapsReturnsTrueForSameDateAndIntersectingIntervals() {
        ActivityScheduleOverride first = new ActivityScheduleOverride();
        first.setDate(LocalDate.of(2026, 4, 15));
        first.setStartTime(LocalTime.of(10, 0));
        first.setEndTime(LocalTime.of(11, 0));
        first.setType(ScheduleOverrideType.AVAILABLE);

        ActivityScheduleOverride second = new ActivityScheduleOverride();
        second.setDate(LocalDate.of(2026, 4, 15));
        second.setStartTime(LocalTime.of(10, 30));
        second.setEndTime(LocalTime.of(11, 30));
        second.setType(ScheduleOverrideType.UNAVAILABLE);

        assertTrue(first.overlaps(second));
    }

    @Test
    void overlapsReturnsFalseForDifferentDates() {
        ActivityScheduleOverride first = new ActivityScheduleOverride();
        first.setDate(LocalDate.of(2026, 4, 15));
        first.setStartTime(LocalTime.of(10, 0));
        first.setEndTime(LocalTime.of(11, 0));
        first.setType(ScheduleOverrideType.AVAILABLE);

        ActivityScheduleOverride second = new ActivityScheduleOverride();
        second.setDate(LocalDate.of(2026, 4, 16));
        second.setStartTime(LocalTime.of(10, 30));
        second.setEndTime(LocalTime.of(11, 30));
        second.setType(ScheduleOverrideType.UNAVAILABLE);

        assertFalse(first.overlaps(second));
    }
}