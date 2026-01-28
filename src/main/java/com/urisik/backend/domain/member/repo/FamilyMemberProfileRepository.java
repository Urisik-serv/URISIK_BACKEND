package com.urisik.backend.domain.member.repo;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.enums.FamilyRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FamilyMemberProfileRepository extends JpaRepository<FamilyMemberProfile, Long> {


    Optional<FamilyMemberProfile> findByMember_Id(Long memberId);

    List<FamilyMemberProfile> findAllByFamilyRoom_Id(Long FamilyRoomId);



    @EntityGraph(attributePaths = {"familyRoom"})
    Optional<FamilyMemberProfile> findByFamilyRoom_IdAndMember_Id(Long familyRoomId, Long memberId);


    @EntityGraph(attributePaths = {"familyRoom"})
    Optional<FamilyMemberProfile> findByFamilyRoom_IdAndId(Long familyRoomId, Long profileId);


    @EntityGraph(attributePaths = {"familyRoom", "member"})
    @Query("select p from FamilyMemberProfile p")
    List<FamilyMemberProfile> findAllWithFamilyRoom();


    boolean existsByFamilyRoom_IdAndFamilyRoleAndIdNot(
            Long familyRoomId,
            FamilyRole role,
            Long excludeProfileId
    );
}
