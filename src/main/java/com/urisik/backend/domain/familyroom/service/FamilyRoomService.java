package com.urisik.backend.domain.familyroom.service;

import com.urisik.backend.domain.familyroom.dto.req.CreateFamilyRoomReqDTO;
import com.urisik.backend.domain.familyroom.dto.res.CreateFamilyRoomResDTO;
import com.urisik.backend.domain.familyroom.dto.res.ReadFamilyRoomContextResDTO;
import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.familyroom.enums.FamilyPolicy;
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

    /** 가족방 생성 */
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

        // 생성자는 자동 참여 (member는 영속 상태이므로 dirty checking으로 반영)
        member.setFamilyRoom(saved);

        return new CreateFamilyRoomResDTO(saved.getId());
    }

    /** 가족방 컨텍스트 조회 */
    @Transactional(readOnly = true)
    public ReadFamilyRoomContextResDTO readMyFamilyRoomContext(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.MEMBER_NOT_FOUND));

        // 소속 가족방이 없으면 컨텍스트 조회 불가
        if (member.getFamilyRoom() == null) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM_NOT_FOUND);
        }

        FamilyRoom familyRoom = member.getFamilyRoom();

        // 프로필 생성 이후 사용 (해당 familyRoom 소속 프로필만 조회)
        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoom.getId(), memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NO_PROFILE_IN_FAMILY));

        FamilyRole role = profile.getFamilyRole(); // "MOM" / "DAD" / "GRANDMOTHER" / "GRANDFATHER" / "SON" / "DAUGHTER"
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

        // 엄마/아빠/할머니/할아버지 중 하나는 반드시 존재
        boolean hasMother = req.familyComposition().hasMother();
        boolean hasFather = req.familyComposition().hasFather();
        boolean hasGrandMother = req.familyComposition().hasGrandMother();
        boolean hasGrandFather = req.familyComposition().hasGrandFather();

        switch (req.familyPolicy()) {
            case MOTHER_ONLY -> {
                if (!hasMother) throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
            }
            case FATHER_ONLY -> {
                if (!hasFather) throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
            }
            case GRANDMOTHER_ONLY -> {
                if (!hasGrandMother) throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
            }
            case GRANDFATHER_ONLY -> {
                if (!hasGrandFather) throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
            }
            default -> throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
        }
    }

    /**
     * 가족방 FamilyPolicy 조회
     * - FamilyWishList에서 정책만 필요할 때가 있어 메서드로 제공
     */
    @Transactional(readOnly = true)
    public FamilyPolicy getFamilyPolicy(Long familyRoomId) {
        FamilyRoom familyRoom = familyRoomRepository.findById(familyRoomId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM_NOT_FOUND));
        return familyRoom.getFamilyPolicy();
    }

    /**
     * 특정 가족방에서 현재 사용자의 FamilyRole 조회
     * - 가족방컨텍스트 API의 me.familyRole과 동일한 값
     * - 방장 판단은 FamilyPolicy.isLeaderRole(role)로 계산
     */
    @Transactional(readOnly = true)
    public FamilyRole getMyFamilyRole(Long memberId, Long familyRoomId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.MEMBER_NOT_FOUND));

        if (member.getFamilyRoom() == null || !member.getFamilyRoom().getId().equals(familyRoomId)) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM_NOT_FOUND);
        }

        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NO_PROFILE_IN_FAMILY));

        return profile.getFamilyRole();
    }

    /**
     * 방장 검증
     * - 방장 권한은 상태가 아니라, 가족방 정책(FamilyPolicy) + 역할(FamilyRole) 조합으로 항상 계산
     */
    @Transactional(readOnly = true)
    public void validateLeader(Long memberId, Long familyRoomId) {

        // 가족방 조회
        FamilyRoom familyRoom = familyRoomRepository.findById(familyRoomId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM_NOT_FOUND));

        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.NOT_FAMILY_MEMBER));

        FamilyRole role = profile.getFamilyRole();
        boolean isLeader = familyRoom.getFamilyPolicy().isLeaderRole(role);

        if (!isLeader) {
            throw new FamilyRoomException(FamilyRoomErrorCode.NOT_LEADER);
        }
    }

    /** 가족방 멤버 검증 */
    @Transactional(readOnly = true)
    public FamilyMemberProfile validateMember(Long memberId, Long familyRoomId) {
        return familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() ->
                        new FamilyRoomException(FamilyRoomErrorCode.NOT_FAMILY_MEMBER)
                );
    }
}
