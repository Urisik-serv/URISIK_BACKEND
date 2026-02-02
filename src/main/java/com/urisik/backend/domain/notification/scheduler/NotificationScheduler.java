package com.urisik.backend.domain.notification.scheduler;

import com.urisik.backend.domain.mealplan.repository.MealPlanRepository;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.member.enums.AlarmPolicy;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.member.repo.MemberRepository;
import com.urisik.backend.domain.member.service.MemberService;
import com.urisik.backend.domain.notification.enums.NotificationType;
import com.urisik.backend.domain.notification.repository.NotificationRepository;
import com.urisik.backend.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {
    private final FamilyMemberProfileRepository profileRepository;
    private final NotificationService notificationService;
    private final MealPlanRepository mealPlanRepository;
    private NotificationRepository notificationRepository;

    // 1. 매주 토요일 식단 생성 알림
    @Scheduled(cron = "0 0 12 * * *")  // 테스트를 위해 매일 알림이 오도록 설정. 이후 "0 0 12 SAT"로 변경 예정
    public void sendSaturdayReminder() {
        List<FamilyMemberProfile> allMembers = profileRepository.findAllWithFamilyRoom();

        // 방장 권한이 있는 멤버 모아서 한 번에 알림 전송
        List<Member> targets = allMembers.stream()
                .filter(p -> p.getFamilyRoom() != null &&
                        p.getFamilyRoom().getFamilyPolicy().isLeaderRole(p.getFamilyRole()))
                .map(FamilyMemberProfile::getMember)
                .filter(this::isAlarmAgreed)
                .toList();

        if (!targets.isEmpty()) {
            notificationService.sendNotification(
                    targets,
                    NotificationType.MEAL_PLAN_REMINDER,
                    "다음 주 식단을 생성할 시간이에요.");
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
                .filter(this::isAlarmAgreed)
                .toList();

        if (!targets.isEmpty()) {
            notificationService.sendNotification(
                    targets,
                    NotificationType.MEAL_PLAN_REMINDER,
                    "다음 주 식단을 생성할 시간이 얼마 안 남았어요.");
        }
    }

    // 3. 매일 12시에 오늘의 식단 리뷰 요청
    @Scheduled(cron = "0 0 12 * * *")
    public void sendDailyReviewReminder() {
        List<FamilyMemberProfile> profiles = profileRepository.findAllWithFamilyRoom();

        // 모든 가족 구성원 대상
        List<Member> targets = profiles.stream()
                .map(FamilyMemberProfile::getMember)
                .filter(this::isAlarmAgreed)
                .toList();

        if (!targets.isEmpty()) {
            notificationService.sendNotification(
                    targets,
                    NotificationType.REVIEW_REMINDER,
                    "오늘 먹은 메뉴에 대해 리뷰를 작성해요.");
        }
    }


    /**
     * 알림 데이터 관리 -> 매일 새벽 4시에 실행 (초 분 시 일 월 요일)
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void deleteOldNotifications() {
        LocalDateTime deleteDate = LocalDateTime.now().minusDays(7);

        notificationRepository.deleteNotifications(deleteDate);
        log.info("7일 지난 알림 삭제 완료.");

    }

    // 알림 수신 동의 여부 확인 (AlarmPolicy.ALARM_AGREED 인 경우만 true)
    private boolean isAlarmAgreed(Member member) {
        return member != null &&
                AlarmPolicy.ALARM_AGREED.equals(member.getAlarmPolicy());
    }

}