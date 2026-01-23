package com.urisik.backend.domain.member.service;

import com.urisik.backend.domain.allergy.entity.MemberAllergy;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.member.enums.FamilyRole;
import com.urisik.backend.domain.familyroom.repository.FamilyRoomRepository;
import com.urisik.backend.domain.member.converter.FamilyMemberProfileConverter;
import com.urisik.backend.domain.member.dto.req.FamilyMemberProfileRequest;
import com.urisik.backend.domain.member.dto.res.FamilyMemberProfileResponse;
import com.urisik.backend.domain.member.entity.DietPreference;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.member.entity.MemberWishList;
import com.urisik.backend.domain.member.enums.DietPreferenceList;
import com.urisik.backend.domain.member.exception.MemberException;
import com.urisik.backend.domain.member.exception.code.MemberErrorCode;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.member.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyMemberProfileService {

    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final FamilyRoomRepository familyRoomRepository;
    private final MemberRepository memberRepository;

    public FamilyMemberProfileResponse.Create create
            (Long familyRoomId, Long memberId, FamilyMemberProfileRequest.Create req)
    {
        Member member = memberRepository.findWithFamilyRoomById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.No_Member));

        // 1단계  memberId가 가진 가족방 가져와서 안에 해당 가족방이 있는지 확인. 없다면 오류.
        FamilyRoom familyRoom = member.getFamilyRoom();
        if (familyRoom == null) {
            throw new MemberException(MemberErrorCode.No_Room);
        }
        if (!familyRoom.getId().equals(familyRoomId)) {
            throw new MemberException(MemberErrorCode.FORBIDDEN_ROOM); // 403 성격 에러코드
        }


        // 2단계 familyRoom에 속한 FamilyMemberProfile의 리스트를 가져온다. 프로필들중 Mom, Father역할을 식별한다
        //Mom과 father 은 한명밖에 없으니 요청과 현재 부모상태 체크하고 통과되면 프로필을 등록한다.

        List<FamilyMemberProfile> familyMemberProfiles = familyMemberProfileRepository.findAllByFamilyRoom_Id(familyRoom.getId());
        FamilyRole requestedRole = req.getRole();

        // 엄마/아빠만 중복 제한
        if (requestedRole == FamilyRole.MOM || requestedRole == FamilyRole.DAD) {

            boolean alreadyExists = familyMemberProfiles.stream()
                    .anyMatch(p -> p.getFamilyRole() == requestedRole);

            if (alreadyExists) {
                throw new MemberException(MemberErrorCode.Already_Exists);
            }
        }
        //3단계 req 정보 저장 FamilyMemberProfile에 저장
        FamilyMemberProfile profile = FamilyMemberProfile.builder()
                .nickname(req.getNickname())
                .member(member)
                .familyRole(req.getRole())
                .likedIngredients(req.getLikedIngredients())
                .dislikedIngredients(req.getDislikedIngredients())
                .familyRoom(familyRoom)
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
        return FamilyMemberProfileConverter.toCreate(saved);
    }

    /*
    -----------------------------------------------------------
     */
    public FamilyMemberProfileResponse.Update update(
            Long familyRoomId, Long memberId, FamilyMemberProfileRequest.Update req){


        return null;
    }

    /*
    --------------------------------------------------------------------
     */

    public FamilyMemberProfileResponse.Detail getMyProfile
            (Long familyRoomId, Long memberId) {

        // 방안에 있는 맴버 찾기.
        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.No_Profile_In_Family));



        return FamilyMemberProfileConverter.toDetail(profile);

    }



}
