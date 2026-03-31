package com.careflow.repository;

import com.careflow.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    boolean existsByProviderId(Long providerId);
}