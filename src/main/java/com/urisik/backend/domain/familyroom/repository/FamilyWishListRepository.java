package com.urisik.backend.domain.familyroom.repository;

import com.urisik.backend.domain.familyroom.entity.FamilyWishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FamilyWishListRepository extends JpaRepository<FamilyWishList, Long> {

    boolean existsByFamilyRoomIdAndFoodId(Long familyRoomId, Long foodId);

    Optional<FamilyWishList> findByFamilyRoomIdAndFoodId(Long familyRoomId, Long foodId);

    List<FamilyWishList> findAllByFamilyRoomId(Long familyRoomId);

    void deleteByFamilyRoomIdAndFoodIdIn(Long familyRoomId, List<Long> foodIds);

    void deleteByFamilyRoomIdAndFoodId(Long familyRoomId, Long foodId);
}
