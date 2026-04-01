package com.careflow.repository;

import com.careflow.model.ProviderInvitation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderInvitationRepository extends JpaRepository<ProviderInvitation, Long> {
	boolean existsByEmailAndUsedFalse(String email);
	Optional<ProviderInvitation> findByToken(String token);
}