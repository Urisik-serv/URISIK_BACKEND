package com.urisik.backend.domain.member.service;

import com.urisik.backend.domain.allergy.entity.MemberAllergy;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.familyroom.entity.FamilyMember;
import com.urisik.backend.domain.familyroom.enums.FamilyStatus;
import com.urisik.backend.domain.familyroom.repository.FamilyMemberRepository;
import com.urisik.backend.domain.member.dto.req.FamilyMemberProfileRequest;
import com.urisik.backend.domain.member.dto.res.FamilyMemberProfileResponse;
import com.urisik.backend.domain.member.entity.DietPreference;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.MemberWishList;
import com.urisik.backend.domain.member.enums.DietPreferenceList;
import com.urisik.backend.domain.member.exception.MemberException;
import com.urisik.backend.domain.member.exception.code.MemberErrorCode;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.urisik.backend.domain.familyroom.entity.QFamilyRoom.familyRoom;

@Service
@RequiredArgsConstructor
public class FamilyMemberProfileService {

    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;

    public FamilyMemberProfileResponse.Create create
            (Long familyRoomId, Long memberId, FamilyMemberProfileRequest.Create req)
    {

        // 1단계  memberId가 가진 가족방List를 가져와서 안에 해당 가족방이 있는지 확인. 없다면 오류.


        // 2단계 familyRoom에서 가족 템플릿 List가져오고, 그중 엄마, 아빠가 사용중 x 확인후 배정.
        //요청의 역할이 가족방내에서 역할이 존재(ACTIVE)인지 확인하고 가져오기
        List<FamilyMember> candidates =
                familyMemberRepository.findAllByFamilyRoom_IdAndFamilyRoleAndStatus(
                        familyRoomId,
                        req.getRole(),
                        FamilyStatus.ACTIVE
                );
        if (candidates.isEmpty()) {
            throw new MemberException(MemberErrorCode.No_Roles);
        }

        //3단계 req 정보 저장 FamilyMemberProfile에 저장

        FamilyMemberProfile profile = FamilyMemberProfile.builder()
                .nickname(req.getNickname())
                .role(req.getRole())
                .likedIngredients(req.getLikedIngredients())
                .dislikedIngredients(req.getDislikedIngredients())
                .familyRoom(familyRoomId)
                .build();


        if (req.getAllergy() != null) {
            for (Allergen allergen : req.getAllergy()) {
                profile.addAllergy(MemberAllergy.of(allergen));
            }
        }

        if (req.getWishItems() != null) {
            for (String item : req.getWishItems()) {
                profile.addWish(MemberWishList.of(item));
            }
        }

        if (req.getDietPreferences() != null) {
            for (DietPreferenceList diet : req.getDietPreferences()) { // ✅ DTO도 Enum이면 이렇게
                profile.addDietPreference(DietPreference.of(diet));
            }
        }

        FamilyMemberProfile saved = familyMemberProfileRepository.save(profile);



        //4단계 프론트에서 성공응답과 함꼐 저장한 정보 전달.
        return null;
    }

    public FamilyMemberProfileResponse.Update update(
            Long familyRoomId, Long memberId, FamilyMemberProfileRequest.Update req){


        return null;
    }



}
