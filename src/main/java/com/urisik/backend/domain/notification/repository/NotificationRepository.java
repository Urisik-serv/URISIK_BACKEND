package com.urisik.backend.domain.notification.repository;

import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Slice<Notification> findAllByMember(Member member, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("delete from Notification n where n.createAt < :deleteDate")
    void deleteNotifications(LocalDateTime deleteDate);

    Optional<Notification> findByIdAndMember(Long id, Member member);
}
