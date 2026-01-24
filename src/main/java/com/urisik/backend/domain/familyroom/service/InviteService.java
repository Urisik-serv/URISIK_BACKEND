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
    private final MemberRepository memberRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;

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

        // 가족방 소속 여부 확인
        if (member.getFamilyRoom() == null ||
                !member.getFamilyRoom().getId().equals(familyRoom.getId())) {
            throw new FamilyRoomException(FamilyRoomErrorCode.INVITE_FORBIDDEN);
        }

        String token = generateUniqueToken();
        LocalDateTime now = LocalDateTime.now();

        Invite invite = Invite.create(
                token,
                familyRoom,
                memberId,
                now.plusDays(DEFAULT_EXPIRE_DAYS)
        );

        inviteRepository.save(invite);

        return CreateInviteResDTO.builder()
                .inviteUrl(INVITE_BASE_URL + token)
                .build();
    }

    private String generateUniqueToken() {
        for (int i = 0; i < 3; i++) {
            String token = UUID.randomUUID().toString().replace("-", "");
            if (!inviteRepository.existsByToken(token)) {
                return token;
            }
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 초대 토큰 조회 (미리보기)
     * - inviterName은 실시간 계산
     *   1) 프로필 nickname
     *   2) member.name
     */
    @Transactional(readOnly = true)
    public ReadInviteResDTO readInvite(String token) {

        Invite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.INVITE_TOKEN_INVALID));

        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new FamilyRoomException(FamilyRoomErrorCode.INVITE_TOKEN_EXPIRED);
        }

        FamilyRoom familyRoom = invite.getFamilyRoom();
        Long inviterMemberId = invite.getInviterMemberId();

        Member inviter = memberRepository.findById(inviterMemberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.MEMBER_NOT_FOUND));

        String inviterName = familyMemberProfileRepository.findByMember_Id(inviterMemberId)
                .map(FamilyMemberProfile::getNickname)
                .filter(n -> n != null && !n.isBlank())
                .orElse(inviter.getName());

        return ReadInviteResDTO.builder()
                .familyRoomId(familyRoom.getId())
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
    public AcceptInviteResDTO acceptInvite(String token, Long memberId) {

        Invite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.INVITE_TOKEN_INVALID));

        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new FamilyRoomException(FamilyRoomErrorCode.INVITE_TOKEN_EXPIRED);
        }

        FamilyRoom familyRoom = invite.getFamilyRoom();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.MEMBER_NOT_FOUND));

        // 이미 가족방에 속해있으면 차단
        if (member.getFamilyRoom() != null) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_MEMBER_ALREADY_JOINED);
        }

        member.setFamilyRoom(familyRoom);
        memberRepository.save(member);

        return AcceptInviteResDTO.builder()
                .familyRoomId(familyRoom.getId())
                .build();
    }
}
