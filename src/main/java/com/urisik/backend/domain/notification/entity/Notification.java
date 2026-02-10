package com.urisik.backend.domain.notification.entity;

import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.notification.enums.NotificationType;
import com.urisik.backend.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notification")
public class Notification extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "meal_plan_generation_count")
    private Integer mealPlanGenerationCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;



    public void updateIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
