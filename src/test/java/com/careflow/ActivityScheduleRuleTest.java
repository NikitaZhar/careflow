package com.careflow;

import com.careflow.model.ActivityScheduleRule;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ActivityScheduleRuleTest {

    @Test
    void hasValidTimeRangeReturnsTrueForValidInterval() {
        ActivityScheduleRule rule = new ActivityScheduleRule();
        rule.setDayOfWeek(DayOfWeek.MONDAY);
        rule.setStartTime(LocalTime.of(10, 0));
        rule.setEndTime(LocalTime.of(11, 0));

        assertTrue(rule.hasValidTimeRange());
    }

    @Test
    void hasValidTimeRangeReturnsFalseForInvalidInterval() {
        ActivityScheduleRule rule = new ActivityScheduleRule();
        rule.setDayOfWeek(DayOfWeek.MONDAY);
        rule.setStartTime(LocalTime.of(11, 0));
        rule.setEndTime(LocalTime.of(10, 0));

        assertFalse(rule.hasValidTimeRange());
    }

    @Test
    void appliesToReturnsTrueForSameDayOfWeek() {
        ActivityScheduleRule rule = new ActivityScheduleRule();
        rule.setDayOfWeek(DayOfWeek.MONDAY);
        rule.setStartTime(LocalTime.of(10, 0));
        rule.setEndTime(LocalTime.of(11, 0));

        assertTrue(rule.appliesTo(LocalDate.of(2026, 4, 13))); // Monday
    }

    @Test
    void overlapsReturnsTrueForSameDayAndIntersectingIntervals() {
        ActivityScheduleRule first = new ActivityScheduleRule();
        first.setDayOfWeek(DayOfWeek.MONDAY);
        first.setStartTime(LocalTime.of(10, 0));
        first.setEndTime(LocalTime.of(11, 0));

        ActivityScheduleRule second = new ActivityScheduleRule();
        second.setDayOfWeek(DayOfWeek.MONDAY);
        second.setStartTime(LocalTime.of(10, 30));
        second.setEndTime(LocalTime.of(11, 30));

        assertTrue(first.overlaps(second));
    }

    @Test
    void overlapsReturnsFalseForDifferentDays() {
        ActivityScheduleRule first = new ActivityScheduleRule();
        first.setDayOfWeek(DayOfWeek.MONDAY);
        first.setStartTime(LocalTime.of(10, 0));
        first.setEndTime(LocalTime.of(11, 0));

        ActivityScheduleRule second = new ActivityScheduleRule();
        second.setDayOfWeek(DayOfWeek.TUESDAY);
        second.setStartTime(LocalTime.of(10, 30));
        second.setEndTime(LocalTime.of(11, 30));

        assertFalse(first.overlaps(second));
    }
}