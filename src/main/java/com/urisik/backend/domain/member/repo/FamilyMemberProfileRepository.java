package com.urisik.backend.domain.member.repo;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.enums.FamilyRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FamilyMemberProfileRepository extends JpaRepository<FamilyMemberProfile, Long> {


    @EntityGraph(attributePaths = {
            "memberWishLists",
            "memberAllergyList",
            "dietPreferenceList"
    })
    Optional<FamilyMemberProfile> findByMember_Id(Long memberId);

    List<FamilyMemberProfile> findAllByFamilyRoom_Id(Long FamilyRoomId);



    @EntityGraph(attributePaths = {
            "memberWishLists",
            "memberAllergyList",
            "dietPreferenceList"
    })
    Optional<FamilyMemberProfile> findWithDetailsByFamilyRoom_IdAndMember_Id(Long familyRoomId, Long memberId);


    @EntityGraph(attributePaths = {"familyRoom"})
    Optional<FamilyMemberProfile> findByFamilyRoom_IdAndMember_Id(Long familyRoomId, Long memberId);


    @EntityGraph(attributePaths = {"familyRoom"})
    Optional<FamilyMemberProfile> findByFamilyRoom_IdAndId(Long familyRoomId, Long profileId);



    boolean existsByFamilyRoom_IdAndFamilyRoleAndIdNot(
            Long familyRoomId,
            FamilyRole role,
            Long excludeProfileId
    );
}
