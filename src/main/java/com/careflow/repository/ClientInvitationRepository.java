package com.careflow.repository;

import com.careflow.model.ClientInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientInvitationRepository extends JpaRepository<ClientInvitation, Long> {

    boolean existsByEmailAndProviderIdAndUsedFalse(String email, Long providerId);

    Optional<ClientInvitation> findByToken(String token);
}