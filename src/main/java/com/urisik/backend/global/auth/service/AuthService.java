package com.urisik.backend.global.auth.service;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.member.repo.MemberRepository;
import com.urisik.backend.domain.member.repo.MemberTransformedRecipeWishRepository;
import com.urisik.backend.domain.member.repo.MemberWishListRepository;
import com.urisik.backend.global.auth.exception.AuthenExcetion;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final MemberWishListRepository memberWishListRepository;
    private final MemberTransformedRecipeWishRepository memberTransformedRecipeWishRepository;

    @Transactional
    public void withdraw(Long memberId) {

        // 1) 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenExcetion(AuthErrorCode.NO_MEMBER));

        // 2) 프로필 조회 (없을 수도 있음)
        FamilyMemberProfile targetProfile =
                familyMemberProfileRepository.findByMember_Id(member.getId()).orElse(null);

        if (targetProfile != null) {

            // 3) wishCount 감소 (일반 레시피)
            List<Long> recipeIds = memberWishListRepository
                    .findMemberWishListsByFamilyMemberProfile_Id(targetProfile.getId())
                    .stream()
                    .map(w -> w.getRecipe().getId())
                    .toList();

            if (!recipeIds.isEmpty()) {
                memberWishListRepository.decreaseWishCount(recipeIds);
            }

            // 4) wishCount 감소 (변형 레시피)
            List<Long> transformedRecipeIds = memberTransformedRecipeWishRepository
                    .findMemberWishListsByFamilyMemberProfile_Id(targetProfile.getId())
                    .stream()
                    .map(w -> w.getRecipe().getId())
                    .toList();

            if (!transformedRecipeIds.isEmpty()) {
                memberTransformedRecipeWishRepository.decreaseWishCount(transformedRecipeIds);
            }

            // ✅ 5) FK 안정적으로: 프로필 먼저 삭제 (자식들 orphanRemoval/cascade로 같이 정리)
            familyMemberProfileRepository.delete(targetProfile);
        }

        // 6) 마지막에 member 삭제
        memberRepository.delete(member);
    }
}