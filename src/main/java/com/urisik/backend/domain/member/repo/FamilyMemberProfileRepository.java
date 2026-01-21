package com.urisik.backend.domain.member.repo;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyMemberProfileRepository extends JpaRepository<FamilyMemberProfile, Long> {

}
