package com.urisik.backend.domain.notification.Listener;

import com.urisik.backend.domain.mealplan.dto.event.MealPlanConfirmedEvent;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.member.enums.AlarmPolicy;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.notification.enums.NotificationType;
import com.urisik.backend.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final FamilyMemberProfileRepository profileRepository;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleMealPlanConfirmed(MealPlanConfirmedEvent event) {
        // 온도 구간이 변경되는 횟수(1, 5, 6, 10회)인지 검사
        if (!List.of(5, 6, 10).contains(event.mealPlanGenerationCount())) {
            return;
        }

        // 알림 대상자 조회 (가족방 멤버 중 알림 동의자만)
        List<Member> targets = profileRepository.findAllByFamilyRoom_Id(event.familyRoomId())
                .stream()
                .map(FamilyMemberProfile::getMember)
                .filter(member -> AlarmPolicy.ALARM_AGREED.equals(member.getAlarmPolicy()))
                .toList();

        // 알림 전송
        if (!targets.isEmpty()) {
            notificationService.sendNotification(
                    targets,
                    NotificationType.TEMPERATURE,
                    "식단 생성 횟수 : " + event.mealPlanGenerationCount());
        }
    }
}
