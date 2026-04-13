package com.careflow;

import com.careflow.exception.AppointmentNotFoundException;
import com.careflow.exception.UserNotFoundException;
import com.careflow.model.Activity;
import com.careflow.model.ActivityStatus;
import com.careflow.model.Appointment;
import com.careflow.model.AppointmentStatus;
import com.careflow.model.User;
import com.careflow.model.UserRole;
import com.careflow.repository.ActivityRepository;
import com.careflow.repository.AppointmentRepository;
import com.careflow.repository.UserRepository;
import com.careflow.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private User provider;
    private User client;
    private User admin;
    private Activity activity;
    private Appointment availableAppointment;
    private Appointment pendingAppointment;
    private Appointment awaitingPaymentAppointment;
    private Appointment confirmedAppointment;

    @BeforeEach
    void setUp() {
        provider = createUserWithId(1L, "provider1", UserRole.PROVIDER);
        client = createUserWithId(2L, "client1", UserRole.CLIENT);
        admin = createUserWithId(3L, "admin1", UserRole.ADMIN);

        activity = createActivityWithId(10L, provider, ActivityStatus.ACTIVE);

        availableAppointment = createAppointmentWithId(
                100L,
                activity,
                null,
                AppointmentStatus.AVAILABLE
        );

        pendingAppointment = createAppointmentWithId(
                101L,
                activity,
                client,
                AppointmentStatus.PENDING
        );

        awaitingPaymentAppointment = createAppointmentWithId(
                102L,
                activity,
                client,
                AppointmentStatus.AWAITING_PAYMENT
        );

        confirmedAppointment = createAppointmentWithId(
                103L,
                activity,
                client,
                AppointmentStatus.CONFIRMED
        );
    }

    @Test
    void reserveAppointment_success() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(availableAppointment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(client));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.reserveAppointment(100L, 2L);

        assertEquals(AppointmentStatus.PENDING, result.getStatus());
        assertNotNull(result.getClient());
        assertEquals(2L, result.getClient().getId());

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());

        Appointment saved = captor.getValue();
        assertEquals(AppointmentStatus.PENDING, saved.getStatus());
        assertEquals(2L, saved.getClient().getId());
    }

    @Test
    void reserveAppointment_failsWhenAppointmentIsNotAvailable() {
        pendingAppointment.setStatus(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(101L)).thenReturn(Optional.of(pendingAppointment));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> appointmentService.reserveAppointment(101L, 2L)
        );

        assertEquals("Only AVAILABLE appointment can be reserved", ex.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void reserveAppointment_failsWhenUserIsNotClient() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(availableAppointment));
        when(userRepository.findById(3L)).thenReturn(Optional.of(admin));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.reserveAppointment(100L, 3L)
        );

        assertEquals("Only CLIENT can reserve appointment", ex.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void reserveAppointment_failsWhenClientReservesOwnProviderAppointment() {
        User samePersonAsProvider = createUserWithId(1L, "provider-as-client", UserRole.CLIENT);

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(availableAppointment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(samePersonAsProvider));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.reserveAppointment(100L, 1L)
        );

        assertEquals("Provider cannot reserve own appointment", ex.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void reserveAppointment_failsWhenAppointmentNotFound() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        AppointmentNotFoundException ex = assertThrows(
                AppointmentNotFoundException.class,
                () -> appointmentService.reserveAppointment(999L, 2L)
        );

        assertEquals("Appointment with id 999 not found", ex.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void reserveAppointment_failsWhenClientNotFound() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(availableAppointment));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(
                UserNotFoundException.class,
                () -> appointmentService.reserveAppointment(100L, 999L)
        );

        assertEquals("User with id 999 not found", ex.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void confirmAppointment_success() {
        when(appointmentRepository.findById(101L)).thenReturn(Optional.of(pendingAppointment));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.confirmAppointment(101L);

        assertEquals(AppointmentStatus.AWAITING_PAYMENT, result.getStatus());
        verify(appointmentRepository).save(pendingAppointment);
    }

    @Test
    void confirmAppointment_failsWhenStatusIsNotPending() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(availableAppointment));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> appointmentService.confirmAppointment(100L)
        );

        assertEquals("Only PENDING appointment can be confirmed", ex.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void rejectAppointment_success() {
        when(appointmentRepository.findById(101L)).thenReturn(Optional.of(pendingAppointment));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.rejectAppointment(101L);

        assertEquals(AppointmentStatus.REJECTED, result.getStatus());
        verify(appointmentRepository).save(pendingAppointment);
    }

    @Test
    void rejectAppointment_failsWhenStatusIsNotPending() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(availableAppointment));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> appointmentService.rejectAppointment(100L)
        );

        assertEquals("Only PENDING appointment can be rejected", ex.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void markAppointmentAsPaid_success() {
        when(appointmentRepository.findById(102L)).thenReturn(Optional.of(awaitingPaymentAppointment));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.markAppointmentAsPaid(102L);

        assertEquals(AppointmentStatus.CONFIRMED, result.getStatus());
        verify(appointmentRepository).save(awaitingPaymentAppointment);
    }

    @Test
    void markAppointmentAsPaid_failsWhenStatusIsNotAwaitingPayment() {
        when(appointmentRepository.findById(101L)).thenReturn(Optional.of(pendingAppointment));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> appointmentService.markAppointmentAsPaid(101L)
        );

        assertEquals("Only AWAITING_PAYMENT appointment can be marked as paid", ex.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void completeAppointment_success() {
        when(appointmentRepository.findById(103L)).thenReturn(Optional.of(confirmedAppointment));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.completeAppointment(103L);

        assertEquals(AppointmentStatus.COMPLETED, result.getStatus());
        verify(appointmentRepository).save(confirmedAppointment);
    }

    @Test
    void completeAppointment_failsWhenStatusIsNotConfirmed() {
        when(appointmentRepository.findById(102L)).thenReturn(Optional.of(awaitingPaymentAppointment));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> appointmentService.completeAppointment(102L)
        );

        assertEquals("Only CONFIRMED appointment can be completed", ex.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    private User createUserWithId(Long id, String username, UserRole role) {
        User user = new User(username, "password123", username + "@example.com", role);
        setId(user, id);
        return user;
    }

    private Activity createActivityWithId(Long id, User provider, ActivityStatus status) {
        Activity activity = new Activity(
                provider,
                "Consultation",
                "Initial consultation",
                java.math.BigDecimal.valueOf(100),
                60,
                status
        );
        setId(activity, id);
        return activity;
    }

    private Appointment createAppointmentWithId(Long id,
                                                Activity activity,
                                                User client,
                                                AppointmentStatus status) {
        Appointment appointment = new Appointment(
                activity,
                client,
                LocalDateTime.of(2026, 4, 20, 10, 0),
                LocalDateTime.of(2026, 4, 20, 11, 0),
                status
        );
        setId(appointment, id);
        return appointment;
    }

    private void setId(Object target, Long id) {
        try {
            Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
    }
}