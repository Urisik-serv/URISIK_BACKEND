package com.urisik.backend.domain.familyroom.service;

import com.urisik.backend.domain.familyroom.dto.req.CreateFamilyRoomReqDTO;
import com.urisik.backend.domain.familyroom.dto.res.CreateFamilyRoomResDTO;
import com.urisik.backend.domain.familyroom.dto.res.ReadFamilyRoomContextResDTO;
import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.familyroom.exception.FamilyRoomException;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomErrorCode;
import com.urisik.backend.domain.familyroom.repository.FamilyRoomRepository;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.member.enums.FamilyRole;
import com.urisik.backend.domain.member.exception.MemberException;
import com.urisik.backend.domain.member.exception.code.MemberErrorCode;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.member.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyRoomService {

    private final FamilyRoomRepository familyRoomRepository;
    private final MemberRepository memberRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;

    /**
     * 가족방 생성
     */
    @Transactional
    public CreateFamilyRoomResDTO createFamilyRoom(Long memberId, CreateFamilyRoomReqDTO req) {
        validate(req);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.MEMBER_NOT_FOUND));

        // 소속 가족방이 있으면 생성 불가
        if (member.getFamilyRoom() != null) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_MEMBER_ALREADY_JOINED);
        }

        // 가족방 엔티티 생성 + 저장
        FamilyRoom room = FamilyRoom.create(req.familyPolicy());
        FamilyRoom saved = familyRoomRepository.save(room);

        // 생성자는 자동 참여
        member.setFamilyRoom(saved);
        memberRepository.save(member);

        return new CreateFamilyRoomResDTO(saved.getId());
    }

    /**
     * 가족방 컨텍스트 조회
     */
    @Transactional(readOnly = true)
    public ReadFamilyRoomContextResDTO readMyFamilyRoomContext(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.MEMBER_NOT_FOUND));

        // 소속 가족방이 없으면 컨텍스트 조회 불가
        if (member.getFamilyRoom() == null) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM_NOT_FOUND);
        }

        FamilyRoom familyRoom = member.getFamilyRoom();

        // 프로필 생성 이후 사용
        FamilyMemberProfile profile = familyMemberProfileRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.No_Profile_In_Family));

        FamilyRole role = profile.getFamilyRole(); // "MOM" / "DAD" / "SON" / "DAUGHTER"
        boolean isLeader = familyRoom.getFamilyPolicy().isLeaderRole(role);

        // 방장 권한 = 위시리스트 수정 + 식단 생성/수정
        ReadFamilyRoomContextResDTO.Capabilities capabilities = ReadFamilyRoomContextResDTO.Capabilities.builder()
                .leader(isLeader)
                .canEditWishlist(isLeader)
                .canCreateMealPlan(isLeader)
                .canEditMealPlan(isLeader)
                .build();

        ReadFamilyRoomContextResDTO.Me me = ReadFamilyRoomContextResDTO.Me.builder()
                .memberId(member.getId())
                .familyRole(role)
                .nickName(profile.getNickname())
                .build();

        // MealPlan은 연결 후에 도메인에서 계산
        return ReadFamilyRoomContextResDTO.builder()
                .familyRoomId(familyRoom.getId())
                .familyPolicy(familyRoom.getFamilyPolicy())
                .me(me)
                .capabilities(capabilities)
                .mealPlanCreated(false)
                .build();
    }

    private void validate(CreateFamilyRoomReqDTO req) {
        if (req == null) throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);

        // familyPolicy
        if (req.familyPolicy() == null) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
        }

        // familyComposition
        if (req.familyComposition() == null) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
        }
        if (req.familyComposition().sonCount() < 0 || req.familyComposition().daughterCount() < 0) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
        }

        // 엄마/아빠 중 하나는 반드시 존재
        boolean hasMother = req.familyComposition().hasMother();
        boolean hasFather = req.familyComposition().hasFather();

        switch (req.familyPolicy()) {
            case MOTHER_ONLY -> {
                if (!hasMother) throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
            }
            case FATHER_ONLY -> {
                if (!hasFather) throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
            }
            default -> throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
        }
    }
}
