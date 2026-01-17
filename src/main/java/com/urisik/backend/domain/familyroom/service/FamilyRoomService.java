package com.urisik.backend.domain.familyroom.service;

import com.urisik.backend.domain.familyroom.dto.req.CreateFamilyRoomReqDTO;
import com.urisik.backend.domain.familyroom.dto.res.CreateFamilyRoomResDTO;
import com.urisik.backend.domain.familyroom.entity.FamilyMember;
import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.familyroom.exception.FamilyRoomException;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomErrorCode;
import com.urisik.backend.domain.familyroom.repository.FamilyMemberRepository;
import com.urisik.backend.domain.familyroom.repository.FamilyRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyRoomService {

    private final FamilyRoomRepository familyRoomRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Transactional
    public CreateFamilyRoomResDTO createFamilyRoom(Long memberId, CreateFamilyRoomReqDTO req) {
        validate(req);

        // 가족방 엔티티 생성 + 저장
        FamilyRoom room = FamilyRoom.create(req.familyName().trim(), req.familyPolicy());
        FamilyRoom saved = familyRoomRepository.save(room);

        // 생성자 가족 멤버십 생성 + 저장
        FamilyMember owner = FamilyMember.createOwner(saved.getId(), memberId, req.familyPolicy());
        familyMemberRepository.save(owner);

        return new CreateFamilyRoomResDTO(saved.getId());
    }

    /**
     * 요청 검증
     * NOTE: hasMother/hasFather와 familyPolicy의 정합성은 최소로만 검증한다.
     * Ex. 방장으로 공동을 선택했는데, 가족 구성원에 엄마아빠 둘 중 한명이라도 없는 경우.
     * Ex. 방장으로 엄마 또는 아빠를 선택했는데, 가족 구성원에 엄마 또는 아빠가 없는 경우.
     * 실제 권한 판단은 프로필 생성 이후에 profile.role + family_policy에서 처리한다.
     */
    private void validate(CreateFamilyRoomReqDTO req) {
        if (req == null) throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);

        // familyName
        if (req.familyName() == null || req.familyName().isBlank()) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
        }
        String familyName = req.familyName().trim();
        if (familyName.length() > 50) { // 스키마 기준
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
        }

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

        // policy ↔ composition 정합성 (UI 입력 기반 최소 검증)
        boolean hasMother = req.familyComposition().hasMother();
        boolean hasFather = req.familyComposition().hasFather();

        switch (req.familyPolicy()) {
            case BOTH_PARENTS -> {
                if (!hasMother || !hasFather) {
                    throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
                }
            }
            case MOTHER_ONLY -> {
                if (!hasMother) {
                    throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
                }
            }
            case FATHER_ONLY -> {
                if (!hasFather) {
                    throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM);
                }
            }
        }

        // 가족명 중복 체크
        if (familyRoomRepository.existsByFamilyName(familyName)) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_NAME_DUPLICATED);
        }
    }
}
