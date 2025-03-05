package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.RoomReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomReviewRepository extends JpaRepository<RoomReview, Integer> {

    //  특정 객실(Room)의 모든 리뷰 조회
    List<RoomReview> findAllByRoom_RoomId(Integer roomId);

    //  특정 회원(Member)이 작성한 모든 리뷰 조회
    List<RoomReview> findAllByMember_MemberId(Integer memberId);

    //  특정 객실(Room)의 리뷰 개수 조회 (평점 계산 시 사용)
    long countByRoom_RoomId(Integer roomId);

    //  특정 객실(Room)의 평균 평점 계산
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM RoomReview r WHERE r.room.roomId = :roomId")
    Double findAverageRatingByRoom_RoomId(Integer roomId);

    //  특정 객실의 최근 3개 리뷰 조회
    List<RoomReview> findTop3ByRoom_RoomIdOrderByReviewDateDesc(Integer roomId);

    // 호텔Id를 토대로 객실리뷰 수 계산
    @Query("SELECT COUNT(rr) FROM RoomReview rr JOIN rr.room r WHERE r.hotelId.hotelId = :hotelId")
    Integer countReviewsByHotelId(Integer hotelId);

}
