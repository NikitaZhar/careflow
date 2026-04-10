package com.careflow;

import com.careflow.model.TimeInterval;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeIntervalLogicTest {

    @Test
    void whenStartBeforeEnd_thenIntervalIsValid() {
        TimeInterval interval = new TimeInterval(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        assertTrue(interval.isValid());
    }

    @Test
    void whenStartEqualsEnd_thenIntervalIsInvalid() {
        TimeInterval interval = new TimeInterval(
                LocalTime.of(10, 0),
                LocalTime.of(10, 0)
        );

        assertFalse(interval.isValid());
    }

    @Test
    void whenStartAfterEnd_thenIntervalIsInvalid() {
        TimeInterval interval = new TimeInterval(
                LocalTime.of(14, 0),
                LocalTime.of(13, 0)
        );

        assertFalse(interval.isValid());
    }

    @Test
    void whenIntervalsOverlap_thenIntersectsReturnsTrue() {
        TimeInterval base = new TimeInterval(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        TimeInterval overlap = new TimeInterval(
                LocalTime.of(10, 30),
                LocalTime.of(11, 30)
        );

        assertTrue(base.intersects(overlap));
        assertTrue(overlap.intersects(base));
    }

    @Test
    void whenIntervalsOnlyTouchBorders_thenIntersectsReturnsFalse() {
        TimeInterval base = new TimeInterval(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        TimeInterval separate = new TimeInterval(
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
        );

        assertFalse(base.intersects(separate));
        assertFalse(separate.intersects(base));
    }

    @Test
    void whenIntervalsAreSeparated_thenIntersectsReturnsFalse() {
        TimeInterval first = new TimeInterval(
                LocalTime.of(8, 0),
                LocalTime.of(9, 0)
        );

        TimeInterval second = new TimeInterval(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        assertFalse(first.intersects(second));
        assertFalse(second.intersects(first));
    }

    @Test
    void whenIntervalsAreEqual_thenIntersectsReturnsTrue() {
        TimeInterval first = new TimeInterval(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        TimeInterval second = new TimeInterval(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        assertTrue(first.intersects(second));
    }

    @Test
    void containsReturnsTrueWhenTimeInsideInterval() {
        TimeInterval interval = new TimeInterval(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        assertTrue(interval.contains(LocalTime.of(10, 30)));
    }

    @Test
    void containsReturnsTrueWhenTimeEqualsStart() {
        TimeInterval interval = new TimeInterval(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        assertTrue(interval.contains(LocalTime.of(10, 0)));
    }

    @Test
    void containsReturnsFalseWhenTimeEqualsEnd() {
        TimeInterval interval = new TimeInterval(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        assertFalse(interval.contains(LocalTime.of(11, 0)));
    }

    @Test
    void durationReturnsCorrectValue() {
        TimeInterval interval = new TimeInterval(
                LocalTime.of(9, 15),
                LocalTime.of(10, 45)
        );

        assertEquals(Duration.ofMinutes(90), interval.duration());
    }

    @Test
    void gettersReturnOriginalValues() {
        LocalTime start = LocalTime.of(9, 15);
        LocalTime end = LocalTime.of(10, 45);

        TimeInterval interval = new TimeInterval(start, end);

        assertEquals(start, interval.getStart());
        assertEquals(end, interval.getEnd());
    }
}