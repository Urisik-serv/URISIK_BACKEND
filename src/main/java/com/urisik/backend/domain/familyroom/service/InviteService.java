package com.urisik.backend.domain.familyroom.service;

import com.urisik.backend.domain.familyroom.dto.res.AcceptInviteResDTO;
import com.urisik.backend.domain.familyroom.dto.res.CreateInviteResDTO;
import com.urisik.backend.domain.familyroom.dto.res.ReadInviteResDTO;
import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.familyroom.entity.Invite;
import com.urisik.backend.domain.familyroom.exception.FamilyRoomException;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomErrorCode;
import com.urisik.backend.domain.familyroom.repository.FamilyRoomRepository;
import com.urisik.backend.domain.familyroom.repository.InviteRepository;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.member.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InviteService {

    private static final int DEFAULT_EXPIRE_DAYS = 7;
    private static final String INVITE_BASE_URL = "https://urisik.app/invite/";

    private final InviteRepository inviteRepository;
    private final FamilyRoomRepository familyRoomRepository;
    private final MemberRepository memberRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;

    @Value("${invite.token.pepper}")
    private String inviteTokenPepper;

    /**
     * 초대 토큰 생성
     * - 가족방에 이미 속한 멤버만 생성 가능
     * - Invite에는 inviterMemberId만 저장
     */
    public CreateInviteResDTO createInvite(Long familyRoomId, Long memberId) {

        FamilyRoom familyRoom = familyRoomRepository.findById(familyRoomId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.MEMBER_NOT_FOUND));

        if (member.getFamilyRoom() == null ||
                !member.getFamilyRoom().getId().equals(familyRoom.getId())) {
            throw new FamilyRoomException(FamilyRoomErrorCode.INVITE_FORBIDDEN);
        }

        String rawToken = generateUniqueToken();
        String tokenHash = hashToken(rawToken);

        Invite invite = Invite.create(
                tokenHash,
                familyRoom,
                memberId,
                LocalDateTime.now().plusDays(DEFAULT_EXPIRE_DAYS)
        );

        inviteRepository.save(invite);

        return CreateInviteResDTO.builder()
                .inviteUrl(INVITE_BASE_URL + rawToken)
                .build();
    }

    /**
     * 초대 토큰 조회 (미리보기)
     * - inviterName은 실시간 계산
     *   1) 프로필 nickname
     *   2) member.name
     */
    @Transactional(readOnly = true)
    public ReadInviteResDTO readInvite(String rawToken) {

        Invite invite = inviteRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.INVITE_TOKEN_INVALID));

        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new FamilyRoomException(FamilyRoomErrorCode.INVITE_TOKEN_EXPIRED);
        }

        Member inviter = memberRepository.findById(invite.getInviterMemberId())
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.MEMBER_NOT_FOUND));

        String inviterName =
                familyMemberProfileRepository.findByMember_Id(inviter.getId())
                        .map(FamilyMemberProfile::getNickname)
                        .filter(n -> n != null && !n.isBlank())
                        .orElse(inviter.getName());

        return ReadInviteResDTO.builder()
                .familyRoomId(invite.getFamilyRoom().getId())
                .inviterName(inviterName)
                .expiresAt(invite.getExpiresAt())
                .isExpired(false)
                .build();
    }

    /**
     * 초대 토큰 수락
     * - 토큰 유효성 / 만료 체크
     * - 이미 다른 가족방에 속해 있으면 예외
     * - Member.familyRoom 세팅
     */
    public AcceptInviteResDTO acceptInvite(String rawToken, Long memberId) {

        Invite invite = inviteRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.INVITE_TOKEN_INVALID));

        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new FamilyRoomException(FamilyRoomErrorCode.INVITE_TOKEN_EXPIRED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.MEMBER_NOT_FOUND));

        if (member.getFamilyRoom() != null) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_MEMBER_ALREADY_JOINED);
        }

        member.setFamilyRoom(invite.getFamilyRoom());

        return AcceptInviteResDTO.builder()
                .familyRoomId(invite.getFamilyRoom().getId())
                .build();
    }

    // ---------------- helpers ----------------

    private String generateUniqueToken() {
        for (int i = 0; i < 3; i++) {
            String token = UUID.randomUUID().toString().replace("-", "");
            if (!inviteRepository.existsByTokenHash(hashToken(token))) {
                return token;
            }
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String hashToken(String token) {
        if (token == null || token.isBlank()) {
            throw new FamilyRoomException(FamilyRoomErrorCode.INVITE_TOKEN_INVALID);
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key =
                    new SecretKeySpec(inviteTokenPepper.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash invite token", e);
        }
    }
}
