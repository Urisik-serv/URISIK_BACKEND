package com.urisik.backend.domain.notification.service;

import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.member.repo.MemberRepository;
import com.urisik.backend.domain.notification.converter.NotificationConverter;
import com.urisik.backend.domain.notification.dto.NotificationResDto;
import com.urisik.backend.domain.notification.entity.Notification;
import com.urisik.backend.domain.notification.enums.NotificationType;
import com.urisik.backend.domain.notification.exception.NotificationErrorCode;
import com.urisik.backend.domain.notification.exception.NotificationException;
import com.urisik.backend.domain.notification.repository.NotificationRepository;
import com.urisik.backend.global.auth.exception.AuthenExcetion;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    /**
     * 1. 알림 전송 메서드
     * @param members
     * @param type
     * @Param data
     */
    @Transactional
    public void sendNotification(List<Member> members, NotificationType type, Object data) {

        List<Notification> notifications = members.stream()
                .map(member -> {
                    Notification.NotificationBuilder builder = Notification.builder()
                            .member(member)
                            .type(type)
                            .isRead(false);

                    // 타입 : TEMPERATURE 이고, data 가 존재하는 경우 식단 생성 횟수 저장
                    if (type == NotificationType.TEMPERATURE && data instanceof Integer count) {
                        builder.mealPlanGenerationCount(count);
                    }

                    return builder.build();
                })
                .toList();

        notificationRepository.saveAll(notifications);

        for (Member member : members) {
            sendSseOnly(member.getId(), type, data);
        }
    }

    /**
     * 2.알림 목록 조회 메서드
     * @param memberId
     * @param size
     */
    @Transactional(readOnly = true)
    public Slice<NotificationResDto> getNotifications(Long memberId, Integer size) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenExcetion(AuthErrorCode.NO_MEMBER));
        Pageable pageable = PageRequest.of(0, size);
        Slice<Notification> notifications = notificationRepository.findAllByMember(member, pageable);

        return NotificationConverter.toNotificationResponseListDto(notifications);


    }


    // 유저별 SSE 연결 객체 저장
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long memberId) {
        SseEmitter emitter = new SseEmitter(15L * 1000 * 60); // SseEmitter 객체 15분 유지
        emitters.put(memberId, emitter);

        // 연결 직후 더미 이벤트를 보내서 503 에러를 방지함
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected!"));
        } catch (IOException e) {
            emitters.remove(memberId);
        }

        // 연결 종료/타임아웃 시 맵에서 제거
        emitter.onCompletion(() -> emitters.remove(memberId));
        emitter.onTimeout(() -> emitters.remove(memberId));

        return emitter;
    }



    // 유저가 접속 중인 경우 (emitter 갹체가 존재하는 경우) - 실시간 전송 메서드
    private void sendSseOnly(Long memberId, NotificationType type, Object data) {
        SseEmitter emitter = emitters.get(memberId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(memberId))
                        .name("notification")
                        .data(data));
            } catch (IOException e) {
                emitters.remove(memberId);
                throw new NotificationException(NotificationErrorCode.NOTIFICATION_SEND_FAILED);
            }
        }
    }


}
