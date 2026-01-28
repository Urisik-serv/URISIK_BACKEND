package com.urisik.backend.domain.notification.repository;

import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Slice<Notification> findAllByMember(Member member, Pageable pageable);
}
