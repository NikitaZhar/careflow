package com.careflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @NotBlank(message = "Title must not be blank")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description must not be blank")
    @Column(nullable = false, length = 2000)
    private String description;

    @NotNull(message = "Price must not be null")
    @Positive(message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Duration must not be null")
    @Positive(message = "Duration must be greater than 0")
    @Column(nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityStatus status;

    public Activity() {
    }

    public Activity(User provider,
                    String title,
                    String description,
                    BigDecimal price,
                    Integer durationMinutes,
                    ActivityStatus status) {
        this.provider = provider;
        this.title = title;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.status = status;
    }

    public boolean belongsTo(User provider) {
        if (provider == null || this.provider == null) {
            return false;
        }

        Long currentProviderId = this.provider.getId();
        Long requestedProviderId = provider.getId();

        if (currentProviderId == null || requestedProviderId == null) {
            return false;
        }

        return currentProviderId.equals(requestedProviderId);
    }

    public boolean isOwnedBy(Long providerId) {
        if (providerId == null || provider == null || provider.getId() == null) {
            return false;
        }

        return provider.getId().equals(providerId);
    }

    public boolean isActive() {
        return status == ActivityStatus.ACTIVE;
    }

    public boolean isInactive() {
        return status == ActivityStatus.INACTIVE;
    }

    public void updateDetails(String title,
                              String description,
                              BigDecimal price,
                              Integer durationMinutes) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
    }

    public void activate() {
        this.status = ActivityStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ActivityStatus.INACTIVE;
    }

    public Long getId() {
        return id;
    }

    public User getProvider() {
        return provider;
    }

    public void setProvider(User provider) {
        this.provider = provider;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public void setStatus(ActivityStatus status) {
        this.status = status;
    }
}