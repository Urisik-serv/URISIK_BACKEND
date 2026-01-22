package com.urisik.backend.domain.familyroom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invite")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "inviter_member_id", nullable = false)
    private Long inviterMemberId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_room_id", nullable = false)
    private FamilyRoom familyRoom;

    public static Invite create(
            String token,
            FamilyRoom familyRoom,
            Long inviterMemberId,
            LocalDateTime expiresAt
    ) {
        return Invite.builder()
                .token(token)
                .familyRoom(familyRoom)
                .inviterMemberId(inviterMemberId)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();
    }
}
