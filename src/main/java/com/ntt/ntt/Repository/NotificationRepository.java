package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByMemberOrderByTimestampDesc(Member member); // 특정 사용자 알림 조회 (최신순)

    // 읽지 않은 알림들을 조회하는 메소드
    List<Notification> findByIsReadFalse();  // '읽지 않은 알림'을 반환

    // 회원탈퇴시 해당 회원에게 온 알림을 삭제하기 위해 추가
    void deleteByMember(Member member);

    int countByIsReadFalse(); // 읽지 않은 알림 개수 조회



}




