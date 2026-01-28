package com.urisik.backend.domain.notification.scheduler;

import com.urisik.backend.domain.mealplan.repository.MealPlanRepository;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.notification.enums.NotificationType;
import com.urisik.backend.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final FamilyMemberProfileRepository profileRepository;
    private final NotificationService notificationService;
    private final MealPlanRepository mealPlanRepository;

    // 1. 매주 토요일 식단 생성 알림
    @Scheduled(cron = "0 0 12 * * *")  // 테스트를 위해 매일 알림이 오도록 설정. 이후 "0 0 12 SAT"로 변경 예정
    public void sendSaturdayReminder() {
        List<FamilyMemberProfile> allMembers = profileRepository.findAllWithFamilyRoom();

        // 방장 권한이 있는 멤버 모아서 한 번에 알림 전송
        List<Member> targets = allMembers.stream()
                .filter(p -> p.getFamilyRoom() != null &&
                        p.getFamilyRoom().getFamilyPolicy().isLeaderRole(p.getFamilyRole()))
                .map(FamilyMemberProfile::getMember)
                .toList();

        if (!targets.isEmpty()) {
            notificationService.sendNotification(targets, NotificationType.MEAL_PLAN_REMINDER);
        }
    }

    // 2. 아직 생성하지 않은 경우에 한해 일요일에 한 번 더 알림 전송
    @Scheduled(cron = "0 0 12 * * SUN")
    public void sendSundayReminder() {
        List<FamilyMemberProfile> profiles = profileRepository.findAllWithFamilyRoom();

        // 내일(월요일) 날짜 계산 (식단 시작 기준일)
        LocalDate nextMonday = LocalDate.now().plusDays(1);

        // 알림 대상자 필터링: 방장 권한이 있고 + 내일자 식단이 없는 경우
        List<Member> targets = profiles.stream()
                .filter(p -> p.getFamilyRoom() != null &&
                        p.getFamilyRoom().getFamilyPolicy().isLeaderRole(p.getFamilyRole()))
                .filter(p -> !mealPlanRepository.existsByFamilyRoomIdAndWeekStartDate(
                        p.getFamilyRoom().getId(), nextMonday))
                .map(FamilyMemberProfile::getMember)
                .toList();

        if (!targets.isEmpty()) {
            notificationService.sendNotification(targets, NotificationType.MEAL_PLAN_REMINDER);
        }
    }

    // 3. 매일 저녁 6시 식단 리뷰 요청
    @Scheduled(cron = "0 0 18 * * *")
    public void sendDailyReviewReminder() {
        List<FamilyMemberProfile> profiles = profileRepository.findAllWithFamilyRoom();

        // 모든 가족 구성원 대상
        List<Member> targets = profiles.stream()
                .map(FamilyMemberProfile::getMember)
                .toList();

        if (!targets.isEmpty()) {
            notificationService.sendNotification(targets, NotificationType.REVIEW_REMINDER);
        }
    }
}