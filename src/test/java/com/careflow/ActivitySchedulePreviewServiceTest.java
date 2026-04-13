package com.careflow;

import com.careflow.model.ActivityScheduleRule;
import com.careflow.service.ActivitySchedulePreviewService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ActivitySchedulePreviewServiceTest {

    private final ActivitySchedulePreviewService previewService = new ActivitySchedulePreviewService();

    @Test
    void buildRulePreviewDatesReturnsPreviewForEachRule() {
        ActivityScheduleRule first = createRuleWithId(1L, DayOfWeek.MONDAY);
        ActivityScheduleRule second = createRuleWithId(2L, DayOfWeek.WEDNESDAY);

        Map<Long, String> previewDates = previewService.buildRulePreviewDates(List.of(first, second));

        assertEquals(2, previewDates.size());

        assertTrue(previewDates.containsKey(1L));
        assertTrue(previewDates.containsKey(2L));

        assertNotNull(previewDates.get(1L));
        assertNotNull(previewDates.get(2L));

        assertFalse(previewDates.get(1L).isBlank());
        assertFalse(previewDates.get(2L).isBlank());

        assertEquals(3, previewDates.get(1L).split(", ").length);
        assertEquals(3, previewDates.get(2L).split(", ").length);
    }

    @Test
    void buildRulePreviewDatesReturnsEmptyMapForEmptyRules() {
        Map<Long, String> previewDates = previewService.buildRulePreviewDates(List.of());

        assertNotNull(previewDates);
        assertTrue(previewDates.isEmpty());
    }

    @Test
    void buildNextDatesTextReturnsCommaSeparatedDates() {
        String result = previewService.buildNextDatesText(DayOfWeek.MONDAY, 3);

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertEquals(3, result.split(", ").length);
    }

    @Test
    void buildNextDatesTextReturnsSingleDateWhenCountIsOne() {
        String result = previewService.buildNextDatesText(DayOfWeek.FRIDAY, 1);

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertEquals(1, result.split(", ").length);
        assertFalse(result.contains(", "));
    }

    private ActivityScheduleRule createRuleWithId(Long id, DayOfWeek dayOfWeek) {
        ActivityScheduleRule rule = new ActivityScheduleRule();
        setRuleId(rule, id);
        rule.setDayOfWeek(dayOfWeek);
        rule.setStartTime(LocalTime.of(10, 0));
        rule.setEndTime(LocalTime.of(11, 0));
        rule.setActive(true);
        return rule;
    }

    private void setRuleId(ActivityScheduleRule rule, Long id) {
        try {
            Field idField = ActivityScheduleRule.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(rule, id);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось установить id правилу расписания", e);
        }
    }
}