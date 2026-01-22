package com.urisik.backend.domain.familyroom.service;

import com.urisik.backend.domain.familyroom.dto.res.AcceptInviteResDTO;
import com.urisik.backend.domain.familyroom.dto.res.CreateInviteResDTO;
import com.urisik.backend.domain.familyroom.dto.res.ReadInviteResDTO;
import com.urisik.backend.domain.familyroom.entity.FamilyMember;
import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.familyroom.entity.Invite;
import com.urisik.backend.domain.familyroom.exception.FamilyRoomException;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomErrorCode;
import com.urisik.backend.domain.familyroom.repository.FamilyMemberRepository;
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
    private final FamilyMemberRepository familyMemberRepository;
    private final MemberRepository memberRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;

    /**
     * 초대 토큰 생성
     * - 가족방 멤버라면 누구나 생성 가능
     * - Invite에는 inviterMemberId만 저장
     */
    public CreateInviteResDTO createInvite(Long familyRoomId, Long memberId) {

        FamilyRoom familyRoom = familyRoomRepository.findById(familyRoomId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.MEMBER_NOT_FOUND));

        boolean isMember = familyMemberRepository.existsByFamilyRoomAndMember(familyRoom, member);
        if (!isMember) {
            throw new FamilyRoomException(FamilyRoomErrorCode.INVITE_FORBIDDEN);
        }

        String token = generateUniqueToken();
        LocalDateTime now = LocalDateTime.now();

        Invite invite = Invite.create(
                token,
                familyRoom,
                memberId, // inviterMemberId 저장
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
            if (!inviteRepository.existsByToken(token)) return token;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 초대 토큰 조회 (미리보기)
     * - inviterName은 실시간 계산:
     *   profile.nickname(있으면) 우선, 없으면 member.member_name
     */
    @Transactional(readOnly = true)
    public ReadInviteResDTO readInvite(String token) {

        Invite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.INVITE_TOKEN_INVALID));

        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new FamilyRoomException(FamilyRoomErrorCode.INVITE_TOKEN_EXPIRED);
        }

        FamilyRoom familyRoom = invite.getFamilyRoom();
        if (familyRoom == null) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM_NOT_FOUND);
        }

        Long inviterMemberId = invite.getInviterMemberId();
        if (inviterMemberId == null) {
            throw new IllegalStateException("Invite.inviterMemberId is null");
        }

        Member inviterMember = memberRepository.findById(inviterMemberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.MEMBER_NOT_FOUND));

        String inviterName = familyMemberProfileRepository.findByMember_Id(inviterMemberId)
                .map(FamilyMemberProfile::getNickname)
                .filter(nick -> nick != null && !nick.isBlank())
                .orElse(inviterMember.getName()); // member_name

        return ReadInviteResDTO.builder()
                .familyRoomId(familyRoom.getId())
                .inviterName(inviterName)
                .expiresAt(invite.getExpiresAt())
                .isExpired(false)
                .build();
    }

    /**
     * 초대 토큰 수락
     * - 토큰 유효성/만료 체크
     * - 이미 멤버면 예외
     * - family_role은 프로필 생성 시점에 확정 (수락 시점에는 미확정)
     * - family_policy는 가족방 생성 시 선택된 값을 그대로 사용
     */
    public AcceptInviteResDTO acceptInvite(String token, Long memberId) {

        Invite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.INVITE_TOKEN_INVALID));

        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new FamilyRoomException(FamilyRoomErrorCode.INVITE_TOKEN_EXPIRED);
        }

        FamilyRoom familyRoom = invite.getFamilyRoom();
        if (familyRoom == null) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_ROOM_NOT_FOUND);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.MEMBER_NOT_FOUND));

        boolean alreadyMember = familyMemberRepository.existsByFamilyRoomAndMember(familyRoom, member);
        if (alreadyMember) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_MEMBER_ALREADY_JOINED);
        }

        FamilyMember familyMember = FamilyMember.createInvitedMember(
                familyRoom,
                member,
                familyRoom.getFamilyPolicy()
        );

        familyMemberRepository.save(familyMember);

        return AcceptInviteResDTO.builder()
                .familyRoomId(familyRoom.getId())
                .build();
    }
}
