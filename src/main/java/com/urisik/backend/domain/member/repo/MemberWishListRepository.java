package com.urisik.backend.domain.member.repo;

import com.urisik.backend.domain.member.entity.MemberWishList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MemberWishListRepository extends JpaRepository<MemberWishList, Long> {


    @Query("""
    select w from MemberWishList w
    join fetch w.recipe r
    where w.familyMemberProfile.id = :profileId
    order by w.id desc
""")
    List<MemberWishList> findFirstPage(Long profileId, Pageable pageable);

    @Query("""
    select w from MemberWishList w
    join fetch w.recipe r
    where w.familyMemberProfile.id = :profileId
      and w.id < :cursor
    order by w.id desc
""")
    List<MemberWishList> findNextPage(Long profileId, Long cursor, Pageable pageable);

}
