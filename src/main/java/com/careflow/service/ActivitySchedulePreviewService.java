package com.careflow.service;

import com.careflow.model.ActivityScheduleRule;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivitySchedulePreviewService {

    private static final DateTimeFormatter PREVIEW_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM");

    public Map<Long, String> buildRulePreviewDates(List<ActivityScheduleRule> rules) {
        Map<Long, String> previewDates = new LinkedHashMap<>();

        for (ActivityScheduleRule rule : rules) {
            previewDates.put(rule.getId(), buildNextDatesText(rule.getDayOfWeek(), 3));
        }

        return previewDates;
    }

    public String buildNextDatesText(DayOfWeek dayOfWeek, int count) {
        LocalDate today = LocalDate.now();
        StringBuilder result = new StringBuilder();

        LocalDate currentDate = today;
        int found = 0;

        while (found < count) {
            if (currentDate.getDayOfWeek().equals(dayOfWeek)) {
                appendDate(result, currentDate);
                found++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return result.toString();
    }

    private void appendDate(StringBuilder result, LocalDate date) {
        if (!result.isEmpty()) {
            result.append(", ");
        }
        result.append(date.format(PREVIEW_DATE_FORMATTER));
    }
}