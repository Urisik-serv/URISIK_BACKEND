package com.urisik.backend.domain.familyroom.repository;

import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRoomRepository extends JpaRepository<FamilyRoom, Long> {
    boolean existsByFamilyName(String familyName);
}
