package com.urisik.backend.domain.member.repo;

import com.urisik.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByCredentialId(String credentialId);

    @EntityGraph(attributePaths = "familyRoom")
    Optional<Member> findWithFamilyRoomById(Long memberId);
}
