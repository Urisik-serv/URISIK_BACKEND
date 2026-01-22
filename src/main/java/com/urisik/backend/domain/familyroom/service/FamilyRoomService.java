package com.urisik.backend.domain.familyroom.service;

import com.urisik.backend.domain.familyroom.dto.req.CreateFamilyRoomReqDTO;
import com.urisik.backend.domain.familyroom.dto.res.CreateFamilyRoomResDTO;
import com.urisik.backend.domain.familyroom.entity.FamilyMember;
import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.familyroom.exception.FamilyRoomException;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomErrorCode;
import com.urisik.backend.domain.familyroom.repository.FamilyMemberRepository;
import com.urisik.backend.domain.familyroom.repository.FamilyRoomRepository;
import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.member.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyRoomService {

    private final FamilyRoomRepository familyRoomRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CreateFamilyRoomResDTO createFamilyRoom(Long memberId, CreateFamilyRoomReqDTO req) {
        validate(req);

        // 가족방 엔티티 생성 + 저장
        FamilyRoom room = FamilyRoom.create(req.familyPolicy());
        FamilyRoom saved = familyRoomRepository.save(room);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.MEMBER_NOT_FOUND));

        // 생성자는 자동 참여
        FamilyMember creatorMember = FamilyMember.createMember(saved, member, saved.getFamilyPolicy());
        familyMemberRepository.save(creatorMember);

        return new CreateFamilyRoomResDTO(saved.getId());
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
