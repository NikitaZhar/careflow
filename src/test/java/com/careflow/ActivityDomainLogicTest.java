package com.careflow;

import com.careflow.model.Activity;
import com.careflow.model.ActivityStatus;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ActivityDomainLogicTest {

    @Test
    void belongsToReturnsTrueForSameProvider() {
        User provider = createUserWithId(1L, "provider1");
        Activity activity = createActivity(provider);

        assertTrue(activity.belongsTo(provider));
    }

    @Test
    void belongsToReturnsFalseForDifferentProvider() {
        User owner = createUserWithId(1L, "provider1");
        User anotherProvider = createUserWithId(2L, "provider2");
        Activity activity = createActivity(owner);

        assertFalse(activity.belongsTo(anotherProvider));
    }

    @Test
    void isOwnedByReturnsTrueForSameProviderId() {
        User provider = createUserWithId(10L, "provider1");
        Activity activity = createActivity(provider);

        assertTrue(activity.isOwnedBy(10L));
    }

    @Test
    void isOwnedByReturnsFalseForDifferentProviderId() {
        User provider = createUserWithId(10L, "provider1");
        Activity activity = createActivity(provider);

        assertFalse(activity.isOwnedBy(99L));
    }

    @Test
    void isActiveReturnsTrueForActiveStatus() {
        Activity activity = createActivity(createUserWithId(1L, "provider1"));
        activity.setStatus(ActivityStatus.ACTIVE);

        assertTrue(activity.isActive());
        assertFalse(activity.isInactive());
    }

    @Test
    void isInactiveReturnsTrueForInactiveStatus() {
        Activity activity = createActivity(createUserWithId(1L, "provider1"));
        activity.setStatus(ActivityStatus.INACTIVE);

        assertTrue(activity.isInactive());
        assertFalse(activity.isActive());
    }

    @Test
    void updateDetailsChangesMainFields() {
        Activity activity = createActivity(createUserWithId(1L, "provider1"));

        activity.updateDetails(
                "New title",
                "New description",
                new BigDecimal("150.00"),
                90
        );

        assertEquals("New title", activity.getTitle());
        assertEquals("New description", activity.getDescription());
        assertEquals(new BigDecimal("150.00"), activity.getPrice());
        assertEquals(90, activity.getDurationMinutes());
    }

    @Test
    void activateSetsActiveStatus() {
        Activity activity = createActivity(createUserWithId(1L, "provider1"));
        activity.setStatus(ActivityStatus.INACTIVE);

        activity.activate();

        assertEquals(ActivityStatus.ACTIVE, activity.getStatus());
    }

    @Test
    void deactivateSetsInactiveStatus() {
        Activity activity = createActivity(createUserWithId(1L, "provider1"));
        activity.setStatus(ActivityStatus.ACTIVE);

        activity.deactivate();

        assertEquals(ActivityStatus.INACTIVE, activity.getStatus());
    }

    private Activity createActivity(User provider) {
        Activity activity = new Activity();
        activity.setProvider(provider);
        activity.setTitle("Massage");
        activity.setDescription("Relax massage");
        activity.setPrice(new BigDecimal("100.00"));
        activity.setDurationMinutes(60);
        activity.setStatus(ActivityStatus.ACTIVE);
        return activity;
    }

    private User createUserWithId(Long id, String username) {
        User user = new User(username, "123456", username + "@test.com", UserRole.PROVIDER);
        setUserId(user, id);
        return user;
    }

    private void setUserId(User user, Long id) {
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось установить id пользователю", e);
        }
    }
}