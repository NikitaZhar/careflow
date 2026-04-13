package com.careflow;

import com.careflow.model.*;
import com.careflow.repository.ActivityRepository;
import com.careflow.repository.AppointmentRepository;
import com.careflow.repository.UserRepository;
import com.careflow.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppointmentServiceIntegrationTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Проверяет полный жизненный цикл записи:
     * AVAILABLE -> PENDING -> AWAITING_PAYMENT -> CONFIRMED -> COMPLETED
     */
    @Test
    void fullAppointmentLifecycle_success() {
        User provider = userRepository.save(
                new User("provider_it_1", "password123", "provider_it_1@example.com", UserRole.PROVIDER)
        );

        User client = userRepository.save(
                new User("client_it_1", "password123", "client_it_1@example.com", UserRole.CLIENT)
        );

        Activity activity = activityRepository.save(
                new Activity(
                        provider,
                        "Consultation",
                        "Initial consultation",
                        BigDecimal.valueOf(100),
                        60,
                        ActivityStatus.ACTIVE
                )
        );

        Appointment appointment = appointmentRepository.save(
                new Appointment(
                        activity,
                        null,
                        LocalDateTime.of(2026, 4, 20, 10, 0),
                        LocalDateTime.of(2026, 4, 20, 11, 0),
                        AppointmentStatus.AVAILABLE
                )
        );

        // Резервирование
        Appointment reserved = appointmentService.reserveAppointment(
                appointment.getId(),
                client.getId()
        );
        assertEquals(AppointmentStatus.PENDING, reserved.getStatus());
        assertEquals(client.getId(), reserved.getClient().getId());

        // Подтверждение провайдером
        Appointment confirmedByProvider = appointmentService.confirmAppointment(
                appointment.getId()
        );
        assertEquals(AppointmentStatus.AWAITING_PAYMENT, confirmedByProvider.getStatus());

        // Оплата клиентом
        Appointment paid = appointmentService.markAppointmentAsPaid(
                appointment.getId()
        );
        assertEquals(AppointmentStatus.CONFIRMED, paid.getStatus());

        // Завершение услуги
        Appointment completed = appointmentService.completeAppointment(
                appointment.getId()
        );
        assertEquals(AppointmentStatus.COMPLETED, completed.getStatus());

        // Проверка состояния в БД
        Appointment fromDb = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertEquals(AppointmentStatus.COMPLETED, fromDb.getStatus());
        assertEquals(client.getId(), fromDb.getClient().getId());
    }

    /**
     * Проверяет отклонение запроса провайдером:
     * PENDING -> REJECTED
     */
    @Test
    void rejectAppointment_success() {
        User provider = userRepository.save(
                new User("provider_it_2", "password123", "provider_it_2@example.com", UserRole.PROVIDER)
        );

        User client = userRepository.save(
                new User("client_it_2", "password123", "client_it_2@example.com", UserRole.CLIENT)
        );

        Activity activity = activityRepository.save(
                new Activity(
                        provider,
                        "Massage",
                        "Relax massage",
                        BigDecimal.valueOf(80),
                        45,
                        ActivityStatus.ACTIVE
                )
        );

        Appointment appointment = appointmentRepository.save(
                new Appointment(
                        activity,
                        client,
                        LocalDateTime.of(2026, 4, 21, 12, 0),
                        LocalDateTime.of(2026, 4, 21, 12, 45),
                        AppointmentStatus.PENDING
                )
        );

        Appointment rejected = appointmentService.rejectAppointment(appointment.getId());
        assertEquals(AppointmentStatus.REJECTED, rejected.getStatus());

        Appointment fromDb = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertEquals(AppointmentStatus.REJECTED, fromDb.getStatus());
    }

    /**
     * Проверяет, что резервирование невозможно пользователем,
     * который не имеет роли CLIENT.
     */
    @Test
    void reserveAppointment_failsForNonClientUser() {
        User provider = userRepository.save(
                new User("provider_it_3", "password123", "provider_it_3@example.com", UserRole.PROVIDER)
        );

        User admin = userRepository.save(
                new User("admin_it_3", "password123", "admin_it_3@example.com", UserRole.ADMIN)
        );

        Activity activity = activityRepository.save(
                new Activity(
                        provider,
                        "Diagnostics",
                        "Basic diagnostics",
                        BigDecimal.valueOf(120),
                        30,
                        ActivityStatus.ACTIVE
                )
        );

        Appointment appointment = appointmentRepository.save(
                new Appointment(
                        activity,
                        null,
                        LocalDateTime.of(2026, 4, 22, 9, 0),
                        LocalDateTime.of(2026, 4, 22, 9, 30),
                        AppointmentStatus.AVAILABLE
                )
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.reserveAppointment(
                        appointment.getId(),
                        admin.getId()
                )
        );

        assertEquals("Only CLIENT can reserve appointment", ex.getMessage());

        Appointment fromDb = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertEquals(AppointmentStatus.AVAILABLE, fromDb.getStatus());
        assertNull(fromDb.getClient());
    }

    /**
     * Проверяет, что невозможно резервировать термин,
     * если он не находится в статусе AVAILABLE.
     */
    @Test
    void reserveAppointment_failsWhenAppointmentIsNotAvailable() {
        User provider = userRepository.save(
                new User("provider_it_4", "password123", "provider_it_4@example.com", UserRole.PROVIDER)
        );

        User client = userRepository.save(
                new User("client_it_4", "password123", "client_it_4@example.com", UserRole.CLIENT)
        );

        Activity activity = activityRepository.save(
                new Activity(
                        provider,
                        "Therapy",
                        "Therapy session",
                        BigDecimal.valueOf(150),
                        60,
                        ActivityStatus.ACTIVE
                )
        );

        Appointment appointment = appointmentRepository.save(
                new Appointment(
                        activity,
                        client,
                        LocalDateTime.of(2026, 4, 23, 14, 0),
                        LocalDateTime.of(2026, 4, 23, 15, 0),
                        AppointmentStatus.PENDING
                )
        );

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> appointmentService.reserveAppointment(
                        appointment.getId(),
                        client.getId()
                )
        );

        assertEquals("Only AVAILABLE appointment can be reserved", ex.getMessage());

        Appointment fromDb = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertEquals(AppointmentStatus.PENDING, fromDb.getStatus());
        assertEquals(client.getId(), fromDb.getClient().getId());
    }

    /**
     * Проверяет, что подтверждение невозможно,
     * если статус записи не PENDING.
     */
    @Test
    void confirmAppointment_failsWhenStatusIsNotPending() {
        User provider = userRepository.save(
                new User("provider_it_5", "password123", "provider_it_5@example.com", UserRole.PROVIDER)
        );

        Activity activity = activityRepository.save(
                new Activity(
                        provider,
                        "Consultation 2",
                        "Follow-up consultation",
                        BigDecimal.valueOf(90),
                        30,
                        ActivityStatus.ACTIVE
                )
        );

        Appointment appointment = appointmentRepository.save(
                new Appointment(
                        activity,
                        null,
                        LocalDateTime.of(2026, 4, 24, 10, 0),
                        LocalDateTime.of(2026, 4, 24, 10, 30),
                        AppointmentStatus.AVAILABLE
                )
        );

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> appointmentService.confirmAppointment(appointment.getId())
        );

        assertEquals("Only PENDING appointment can be confirmed", ex.getMessage());

        Appointment fromDb = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertEquals(AppointmentStatus.AVAILABLE, fromDb.getStatus());
    }
}