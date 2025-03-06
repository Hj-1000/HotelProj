package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.RoomReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomReviewRepository extends JpaRepository<RoomReview, Integer> {

    // 특정 객실(Room)의 리뷰 목록을 페이징으로 가져오기
    @Query("SELECT r FROM RoomReview r WHERE r.room.roomId = :roomId ORDER BY r.reviewDate DESC")
    Page<RoomReview> findByRoom_RoomIdOrderByReviewDateDesc(@Param("roomId") Integer roomId, Pageable pageable);

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

    // 호텔Id를 토대로 최근 3개 리뷰 조회
    @Query("SELECT rr FROM RoomReview rr JOIN rr.room r WHERE r.hotelId.hotelId = :hotelId ORDER BY rr.reviewDate DESC")
    List<RoomReview> findTop3ByHotelIdOrderByReviewDateDesc(@Param("hotelId") Integer hotelId, Pageable pageable);

}
