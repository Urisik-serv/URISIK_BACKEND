package com.urisik.backend.domain.member.repo;

import com.urisik.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByCredentialId(String credentialId);
    Member findById(long id);
}
