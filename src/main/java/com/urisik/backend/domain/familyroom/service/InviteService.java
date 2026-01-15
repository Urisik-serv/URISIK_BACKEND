package com.urisik.backend.domain.familyroom.service;

import com.urisik.backend.domain.familyroom.dto.res.CreateInviteResDTO;
import com.urisik.backend.domain.familyroom.dto.res.ReadInviteResDTO;
import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.familyroom.entity.Invite;
import com.urisik.backend.domain.familyroom.exception.FamilyRoomException;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomErrorCode;
import com.urisik.backend.domain.familyroom.repository.FamilyMemberRepository;
import com.urisik.backend.domain.familyroom.repository.FamilyRoomRepository;
import com.urisik.backend.domain.familyroom.repository.InviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InviteService {

    private static final int DEFAULT_EXPIRE_DAYS = 7;
    private static final String INVITE_BASE_URL = "https://urisik.app/invite/";

    private final InviteRepository inviteRepository;
    private final FamilyRoomRepository familyRoomRepository;
    private final FamilyMemberRepository familyMemberRepository;

    // 초대 토큰 생성 로직
    public CreateInviteResDTO createInvite(Long familyRoomId, Long memberId) {
        FamilyRoom familyRoom = familyRoomRepository.findById(familyRoomId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM));

        boolean isMember = familyMemberRepository.existsByFamilyRoomIdAndMemberId(familyRoomId, memberId);
        if (!isMember) {
            throw new FamilyRoomException(FamilyRoomErrorCode.INVITE_FORBIDDEN);
        }

        String token = generateUniqueToken();
        LocalDateTime now = LocalDateTime.now();

        Invite invite = Invite.builder()
                .token(token)
                .createdAt(now)
                .expiresAt(now.plusDays(DEFAULT_EXPIRE_DAYS))
                .familyRoom(familyRoom)
                .build();

        inviteRepository.save(invite);

        return CreateInviteResDTO.builder()
                .inviteUrl(INVITE_BASE_URL + token)
                .build();
    }

    private String generateUniqueToken() {
        // 충돌 체크
        for (int i = 0; i < 3; i++) {
            String token = UUID.randomUUID().toString().replace("-", "");
            if (!inviteRepository.existsByToken(token)) return token;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    // 초대 토큰 조회 로직
    @Transactional(readOnly = true)
    public ReadInviteResDTO readInvite(String token) {

        Invite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new FamilyRoomException(
                        FamilyRoomErrorCode.INVITE_TOKEN_INVALID
                ));

        // 만료 여부 체크
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new FamilyRoomException(
                    FamilyRoomErrorCode.INVITE_TOKEN_EXPIRED
            );
        }

        FamilyRoom familyRoom = invite.getFamilyRoom();
        if (familyRoom == null) {
            throw new FamilyRoomException(
                    FamilyRoomErrorCode.FAMILY_ROOM
            );
        }

        return ReadInviteResDTO.builder()
                .familyRoomId(familyRoom.getId())
                .familyName(familyRoom.getFamilyName())
                .expiresAt(invite.getExpiresAt())
                .isExpired(false)
                .build();
    }
}
