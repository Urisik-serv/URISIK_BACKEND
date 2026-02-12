package com.urisik.backend.domain.allergy.repository;

import com.urisik.backend.domain.allergy.entity.MemberAllergy;
import com.urisik.backend.domain.allergy.enums.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberAllergyRepository extends JpaRepository<MemberAllergy, Long> {

    // FamilyMemberProfile -> FamilyRoom 조인 (너 구조에 맞춘 예시)
    @Query("""
        select ma
        from MemberAllergy ma
        join ma.familyMemberProfile fmp
        join fmp.familyRoom fr
        where fr.id = :familyRoomId
    """)
    List<MemberAllergy> findByFamilyRoomId(Long familyRoomId);


    List<MemberAllergy> findByFamilyMemberProfile_Id(Long profileId);

    void deleteAllByFamilyMemberProfile_Id(Long familyMemberProfileId);

    @Query("""
        select distinct ma.allergen
        from MemberAllergy ma
        join ma.familyMemberProfile p
        where p.familyRoom.id = :familyRoomId
    """)
    List<Allergen> findDistinctAllergensByFamilyRoomId(
            @Param("familyRoomId") Long familyRoomId
    );

}

