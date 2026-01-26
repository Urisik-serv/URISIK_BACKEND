package com.urisik.backend.domain.member.service;

import com.urisik.backend.domain.allergy.entity.MemberAllergy;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.repository.MemberAllergyRepository;
import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.member.dto.req.WishListRequest;
import com.urisik.backend.domain.member.dto.res.WishListResponse;
import com.urisik.backend.domain.member.enums.AlarmPolicy;
import com.urisik.backend.domain.member.enums.FamilyRole;
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
import com.urisik.backend.domain.member.repo.DietPreferenceRepository;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.member.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyMemberProfileService {

    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final MemberRepository memberRepository;
    private final MemberAllergyRepository memberAllergyRepository;
    private final DietPreferenceRepository dietPreferenceRepository;

    //

    //post
    @Transactional
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
                throw new MemberException(MemberErrorCode.Already_Exist_Role);
            }
        }
        //3단계 req 정보 저장 FamilyMemberProfile에 저장
        FamilyMemberProfile profile = FamilyMemberProfile.builder()
                .nickname(req.getNickname())
                .alarmPolicy(AlarmPolicy.DISAGREE)
                .member(member)
                .familyRole(req.getRole())
                .profilePicUrl(null)
                .likedIngredients(req.getLikedIngredients())
                .dislikedIngredients(req.getDislikedIngredients())
                .familyRoom(familyRoom)
                .build();

        if (req.getAllergy() != null) {
            for (Allergen allergen : req.getAllergy()) {
                profile.addAllergy(MemberAllergy.of(allergen));
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
    patch
     */

    @Transactional
    public FamilyMemberProfileResponse.Update update(
            Long familyRoomId,
            Long memberId,
            FamilyMemberProfileRequest.Update req
    ) {
        // 1) "프로필"만 조회 (컬렉션 fetch 금지)
        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.No_Profile_In_Family));

        Long profileId = profile.getId();

        // 2) 단일 필드 수정
        if (req.getNickname() != null) {
            profile.setNickname(req.getNickname());
        }

        if (req.getLikedIngredients() != null) {
            profile.setLikedIngredients(req.getLikedIngredients());
        }

        if (req.getDislikedIngredients() != null) {
            profile.setDislikedIngredients(req.getDislikedIngredients());
        }

        // 3) role 변경 + (MOM/DAD) 중복 체크
        if (req.getRole() != null) {
            FamilyRole newRole = req.getRole();

            if (newRole == FamilyRole.MOM || newRole == FamilyRole.DAD) {
                boolean alreadyExists = familyMemberProfileRepository
                        .existsByFamilyRoom_IdAndFamilyRoleAndIdNot(
                                familyRoomId,
                                newRole,
                                profileId
                        );
                if (alreadyExists) {
                    throw new MemberException(MemberErrorCode.Already_Exist_Role);
                }
            }

            profile.setFamilyRole(newRole);
        }

        // 4) allergy 전체 교체 (요청이 null이면 유지)
        if (req.getAllergy() != null) {
            memberAllergyRepository.deleteAllByFamilyMemberProfile_Id(profileId);

            for (Allergen a : req.getAllergy()) {
                MemberAllergy entity = MemberAllergy.of(a);
                entity.setFamilyMemberProfile(profile); // FK 세팅
                memberAllergyRepository.save(entity);
            }
        }

        // 5) dietPreferences 전체 교체 (요청이 null이면 유지)
        if (req.getDietPreferences() != null) {
            dietPreferenceRepository.deleteAllByFamilyMemberProfile_Id(profileId);

            for (DietPreferenceList d : req.getDietPreferences()) {
                DietPreference entity = DietPreference.of(d);
                entity.setFamilyMemberProfile(profile);
                dietPreferenceRepository.save(entity);
            }
        }

        // 6) 응답 조립을 위해 "컬렉션 2번" 조회 (총 3번 조회)
        List<MemberAllergy> allergies = memberAllergyRepository.findByFamilyMemberProfile_Id(profileId);
        List<DietPreference> diets = dietPreferenceRepository.findAllByFamilyMemberProfile_Id(profileId);

        return FamilyMemberProfileConverter.toUpdate(profile, allergies, diets);
    }


    @Transactional
    public FamilyMemberProfileResponse.UpdatePic updatePic(
            Long familyRoomId,
            Long memberId,
            FamilyMemberProfileRequest.UpdatePic req
    ) {
        // 1) 프로필 조회 (memberId + familyRoomId 조건으로 찾는 걸 추천)
        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.No_Profile_In_Family));

        profile.setProfilePicUrl(req.getProfilePicUrl());


        return FamilyMemberProfileResponse.UpdatePic.builder()
                .isSuccess(true)
                .profilePicUrl(profile.getProfilePicUrl())
                .build();
    }

    /*
    --------------------------------------------------------------------
    get
     */


    public FamilyMemberProfileResponse.Detail getMyProfile
            (Long familyRoomId, Long memberId) {

        // 방안에 있는 맴버 찾기.
        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.No_Profile_In_Family));

        Long profileId = profile.getId();

        // 2) 알러지 목록 조회
        List<MemberAllergy> allergies = memberAllergyRepository.findByFamilyMemberProfile_Id(profileId);

        // 3) 식단선호 목록 조회
        List<DietPreference> diets = dietPreferenceRepository.findAllByFamilyMemberProfile_Id(profileId);


        return FamilyMemberProfileConverter.toDetail(profile,allergies,diets);

    }
    /*
    --------------------------------------------------------------------
    delete
     */


    @Transactional
    public FamilyMemberProfileResponse.Delete quitFamilyRoom(Long familyRoomId, Long profileId , Long memberId) {

        FamilyMemberProfile targetProfile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndId(familyRoomId, profileId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.No_Profile_In_Family));

        FamilyMemberProfile requesterProfile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.No_Profile_In_Family));


        //자기 프로필이 거나, 권한이 있을때 삭제
        boolean isSelf = targetProfile.getMember().getId().equals(memberId);

        // 4) 리더 권한 여부 (FamilyPolicy 기준)
        boolean isLeader = requesterProfile.getFamilyRoom()
                .getFamilyPolicy()
                .isLeaderRole(requesterProfile.getFamilyRole());

        // 5) 본인이거나 리더면 삭제 가능
        if (!isSelf && !isLeader) {
            throw new MemberException(MemberErrorCode.FORBIDDEN_Member);
        }

        familyMemberProfileRepository.delete(targetProfile);

        return FamilyMemberProfileResponse.Delete.builder().isSuccess(true).build();

    }





}
