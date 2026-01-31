package com.urisik.backend.domain.familyroom.repository;

import com.urisik.backend.domain.familyroom.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Long> {

    boolean existsByTokenHash(String tokenHash);

    Optional<Invite> findByTokenHash(String tokenHash);
}
