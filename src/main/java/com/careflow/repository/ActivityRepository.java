package com.careflow.repository;

import com.careflow.model.Activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    boolean existsByProviderId(Long providerId);
    List<Activity> findByProviderId(Long providerId);
}