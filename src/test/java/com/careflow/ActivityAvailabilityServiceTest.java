package com.careflow;

import com.careflow.model.Activity;
import com.careflow.model.ActivityScheduleOverride;
import com.careflow.model.ActivityScheduleRule;
import com.careflow.model.ActivityStatus;
import com.careflow.model.ScheduleOverrideType;
import com.careflow.model.TimeInterval;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.repository.ActivityScheduleOverrideRepository;
import com.careflow.repository.ActivityScheduleRuleRepository;
import com.careflow.service.ActivityAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityAvailabilityServiceTest {

    @Mock
    private ActivityScheduleRuleRepository ruleRepository;

    @Mock
    private ActivityScheduleOverrideRepository overrideRepository;

    @InjectMocks
    private ActivityAvailabilityService availabilityService;

    private Activity activity;
    private LocalDate mondayDate;

    @BeforeEach
    void setUp() {
        User provider = new User("provider1", "password123", "provider1@example.com", UserRole.PROVIDER);
        setId(provider, 1L);

        activity = new Activity(
                provider,
                "Consultation",
                "Initial consultation",
                BigDecimal.valueOf(100),
                60,
                ActivityStatus.ACTIVE
        );
        setId(activity, 10L);

        mondayDate = LocalDate.of(2026, 4, 20); // MONDAY
        assertEquals(DayOfWeek.MONDAY, mondayDate.getDayOfWeek());
    }

    @Test
    void getAvailability_returnsBaseIntervalFromActiveRule() {
        ActivityScheduleRule rule = createRule(
                101L,
                activity,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                true
        );

        when(ruleRepository.findByActivityIdAndDayOfWeekAndActiveTrue(activity.getId(), mondayDate.getDayOfWeek()))
                .thenReturn(List.of(rule));
        when(overrideRepository.findByActivityIdAndDate(activity.getId(), mondayDate))
                .thenReturn(List.of());

        List<TimeInterval> result = availabilityService.getAvailability(activity, mondayDate);

        assertEquals(1, result.size());
        assertEquals(LocalTime.of(10, 0), result.get(0).getStart());
        assertEquals(LocalTime.of(12, 0), result.get(0).getEnd());

        verify(ruleRepository).findByActivityIdAndDayOfWeekAndActiveTrue(activity.getId(), DayOfWeek.MONDAY);
        verify(overrideRepository).findByActivityIdAndDate(activity.getId(), mondayDate);
    }

    @Test
    void getAvailability_subtractsUnavailableOverrideFromBaseInterval() {
        ActivityScheduleRule rule = createRule(
                102L,
                activity,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                true
        );

        ActivityScheduleOverride blocked = createOverride(
                activity,
                mondayDate,
                LocalTime.of(11, 0),
                LocalTime.of(12, 30),
                ScheduleOverrideType.UNAVAILABLE
        );

        when(ruleRepository.findByActivityIdAndDayOfWeekAndActiveTrue(activity.getId(), mondayDate.getDayOfWeek()))
                .thenReturn(List.of(rule));
        when(overrideRepository.findByActivityIdAndDate(activity.getId(), mondayDate))
                .thenReturn(List.of(blocked));

        List<TimeInterval> result = availabilityService.getAvailability(activity, mondayDate);

        assertEquals(2, result.size());

        assertEquals(LocalTime.of(10, 0), result.get(0).getStart());
        assertEquals(LocalTime.of(11, 0), result.get(0).getEnd());

        assertEquals(LocalTime.of(12, 30), result.get(1).getStart());
        assertEquals(LocalTime.of(14, 0), result.get(1).getEnd());
    }

    @Test
    void getAvailability_addsAvailableOverrideAsAdditionalInterval() {
        ActivityScheduleRule rule = createRule(
                103L,
                activity,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                true
        );

        ActivityScheduleOverride extraAvailable = createOverride(
                activity,
                mondayDate,
                LocalTime.of(15, 0),
                LocalTime.of(16, 0),
                ScheduleOverrideType.AVAILABLE
        );

        when(ruleRepository.findByActivityIdAndDayOfWeekAndActiveTrue(activity.getId(), mondayDate.getDayOfWeek()))
                .thenReturn(List.of(rule));
        when(overrideRepository.findByActivityIdAndDate(activity.getId(), mondayDate))
                .thenReturn(List.of(extraAvailable));

        List<TimeInterval> result = availabilityService.getAvailability(activity, mondayDate);

        assertEquals(2, result.size());

        assertEquals(LocalTime.of(10, 0), result.get(0).getStart());
        assertEquals(LocalTime.of(12, 0), result.get(0).getEnd());

        assertEquals(LocalTime.of(15, 0), result.get(1).getStart());
        assertEquals(LocalTime.of(16, 0), result.get(1).getEnd());
    }
    
    @Test
    void getAvailability_returnsEmptyListWhenUnavailableOverrideFullyCoversBaseInterval() {
        ActivityScheduleRule rule = createRule(
                104L,
                activity,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                true
        );

        ActivityScheduleOverride blocked = createOverride(
                activity,
                mondayDate,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                ScheduleOverrideType.UNAVAILABLE
        );

        when(ruleRepository.findByActivityIdAndDayOfWeekAndActiveTrue(activity.getId(), mondayDate.getDayOfWeek()))
                .thenReturn(List.of(rule));
        when(overrideRepository.findByActivityIdAndDate(activity.getId(), mondayDate))
                .thenReturn(List.of(blocked));

        List<TimeInterval> result = availabilityService.getAvailability(activity, mondayDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailability_doesNotChangeBaseIntervalWhenUnavailableOverrideDoesNotIntersect() {
        ActivityScheduleRule rule = createRule(
                105L,
                activity,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                true
        );

        ActivityScheduleOverride blocked = createOverride(
                activity,
                mondayDate,
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                ScheduleOverrideType.UNAVAILABLE
        );

        when(ruleRepository.findByActivityIdAndDayOfWeekAndActiveTrue(activity.getId(), mondayDate.getDayOfWeek()))
                .thenReturn(List.of(rule));
        when(overrideRepository.findByActivityIdAndDate(activity.getId(), mondayDate))
                .thenReturn(List.of(blocked));

        List<TimeInterval> result = availabilityService.getAvailability(activity, mondayDate);

        assertEquals(1, result.size());
        assertEquals(LocalTime.of(10, 0), result.get(0).getStart());
        assertEquals(LocalTime.of(12, 0), result.get(0).getEnd());
    }

    @Test
    void getAvailability_returnsMultipleBaseIntervalsWhenSeveralRulesExistForSameDay() {
        ActivityScheduleRule firstRule = createRule(
                106L,
                activity,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                true
        );

        ActivityScheduleRule secondRule = createRule(
                107L,
                activity,
                DayOfWeek.MONDAY,
                LocalTime.of(14, 0),
                LocalTime.of(16, 0),
                true
        );

        when(ruleRepository.findByActivityIdAndDayOfWeekAndActiveTrue(activity.getId(), mondayDate.getDayOfWeek()))
                .thenReturn(List.of(firstRule, secondRule));
        when(overrideRepository.findByActivityIdAndDate(activity.getId(), mondayDate))
                .thenReturn(List.of());

        List<TimeInterval> result = availabilityService.getAvailability(activity, mondayDate);

        assertEquals(2, result.size());

        assertEquals(LocalTime.of(10, 0), result.get(0).getStart());
        assertEquals(LocalTime.of(12, 0), result.get(0).getEnd());

        assertEquals(LocalTime.of(14, 0), result.get(1).getStart());
        assertEquals(LocalTime.of(16, 0), result.get(1).getEnd());
    }

    @Test
    void getAvailability_splitsBaseIntervalWithMultipleUnavailableOverrides() {
        ActivityScheduleRule rule = createRule(
                108L,
                activity,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                true
        );

        ActivityScheduleOverride firstBlocked = createOverride(
                activity,
                mondayDate,
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                ScheduleOverrideType.UNAVAILABLE
        );

        ActivityScheduleOverride secondBlocked = createOverride(
                activity,
                mondayDate,
                LocalTime.of(15, 0),
                LocalTime.of(16, 30),
                ScheduleOverrideType.UNAVAILABLE
        );

        when(ruleRepository.findByActivityIdAndDayOfWeekAndActiveTrue(activity.getId(), mondayDate.getDayOfWeek()))
                .thenReturn(List.of(rule));
        when(overrideRepository.findByActivityIdAndDate(activity.getId(), mondayDate))
                .thenReturn(List.of(firstBlocked, secondBlocked));

        List<TimeInterval> result = availabilityService.getAvailability(activity, mondayDate);

        assertEquals(3, result.size());

        assertEquals(LocalTime.of(10, 0), result.get(0).getStart());
        assertEquals(LocalTime.of(11, 0), result.get(0).getEnd());

        assertEquals(LocalTime.of(12, 0), result.get(1).getStart());
        assertEquals(LocalTime.of(15, 0), result.get(1).getEnd());

        assertEquals(LocalTime.of(16, 30), result.get(2).getStart());
        assertEquals(LocalTime.of(18, 0), result.get(2).getEnd());
    }

    @Test
    void getAvailability_combinesUnavailableAndAvailableOverridesInSameDay() {
        ActivityScheduleRule rule = createRule(
                109L,
                activity,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                true
        );

        ActivityScheduleOverride blocked = createOverride(
                activity,
                mondayDate,
                LocalTime.of(10, 30),
                LocalTime.of(11, 0),
                ScheduleOverrideType.UNAVAILABLE
        );

        ActivityScheduleOverride extraAvailable = createOverride(
                activity,
                mondayDate,
                LocalTime.of(15, 0),
                LocalTime.of(16, 0),
                ScheduleOverrideType.AVAILABLE
        );

        when(ruleRepository.findByActivityIdAndDayOfWeekAndActiveTrue(activity.getId(), mondayDate.getDayOfWeek()))
                .thenReturn(List.of(rule));
        when(overrideRepository.findByActivityIdAndDate(activity.getId(), mondayDate))
                .thenReturn(List.of(blocked, extraAvailable));

        List<TimeInterval> result = availabilityService.getAvailability(activity, mondayDate);

        assertEquals(3, result.size());

        assertEquals(LocalTime.of(10, 0), result.get(0).getStart());
        assertEquals(LocalTime.of(10, 30), result.get(0).getEnd());

        assertEquals(LocalTime.of(11, 0), result.get(1).getStart());
        assertEquals(LocalTime.of(12, 0), result.get(1).getEnd());

        assertEquals(LocalTime.of(15, 0), result.get(2).getStart());
        assertEquals(LocalTime.of(16, 0), result.get(2).getEnd());
    }
    

    private ActivityScheduleRule createRule(Long id,
                                            Activity activity,
                                            DayOfWeek dayOfWeek,
                                            LocalTime startTime,
                                            LocalTime endTime,
                                            boolean active) {
        ActivityScheduleRule rule = new ActivityScheduleRule();
        setId(rule, id);
        rule.setActivity(activity);
        rule.setDayOfWeek(dayOfWeek);
        rule.setStartTime(startTime);
        rule.setEndTime(endTime);
        rule.setActive(active);
        return rule;
    }

    private ActivityScheduleOverride createOverride(Activity activity,
                                                    LocalDate date,
                                                    LocalTime startTime,
                                                    LocalTime endTime,
                                                    ScheduleOverrideType type) {
        ActivityScheduleOverride override = new ActivityScheduleOverride();
        override.setActivity(activity);
        override.setDate(date);
        override.setStartTime(startTime);
        override.setEndTime(endTime);
        override.setType(type);
        return override;
    }

    private void setId(Object target, Long id) {
        try {
            Field idField = target.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось установить id через reflection", e);
        }
    }
}