package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Entity.ServiceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Integer> {
    //검색쿼리
    // 회원 이름으로 검색
    Page<ServiceOrder> findByMember_MemberNameLike(String keyword, Pageable pageable);
    // 회원 이메일로 검색
    Page<ServiceOrder> findByMember_MemberEmailLike(String keyword, Pageable pageable);
    // 예약 방으로 검색
    Page<ServiceOrder> findByReservation_Room_RoomNameLike(String keyword, Pageable pageable);
    // 주문 날짜로 검색
    Page<ServiceOrder> findByRegDateLike(LocalDateTime regDate, Pageable pageable);

    //특정 예약에 해당하는 주문을 조회(멤버이름)
    Page<ServiceOrder> findByReservation_ReservationIdAndMember_MemberNameLike(Integer reservationId, String keyword, Pageable pageable);
    //특정 예약에 해당하는 주문을 조회(멤버이메일)
    Page<ServiceOrder> findByReservation_ReservationIdAndMember_MemberEmailLike(Integer reservationId, String keyword, Pageable pageable);
    //특정 예약에 해당하는 주문을 조회(방이름)
    Page<ServiceOrder> findByReservation_ReservationIdAndReservation_Room_RoomNameLike(Integer reservationId, String keyword, Pageable pageable);
    //특정 예약에 해당하는 주문을 조회(주문날짜)
    Page<ServiceOrder> findByReservation_ReservationIdAndRegDateLike(Integer reservationId, LocalDateTime regDate, Pageable pageable);
    //검색어 없을 때 사용할 조회
    Page<ServiceOrder> findByReservation_ReservationId(Integer reservationId, Pageable pageable);

    //특정 회원의 구매 이력으로 모두 불러오기
    //serviceOrderId를 오름차순으로 정렬하되 status가 CANCELED라면 후순위로 밀기
    @Query("select so from ServiceOrder so where so.member.memberEmail = :memberEmail order by " +
            "case when so.serviceOrderStatus = 'CANCELED' then 1 else 0 end, so.regDate asc")
    public List<ServiceOrder> findServiceOrders(@Param("memberEmail") String memberEmail, Pageable pageable);

    //특정 회원의 주문갯수 검색 메서드
    @Query("select count(so) from ServiceOrder so where so.member.memberEmail = :memberEmail")
    public Integer totalCount(@Param("memberEmail") String memberEmail);

    //예약페이지에 룸서비스 주문금액을 추가하기 위해 쿼리문 추가 2025-02-21
    //이 메서드는 reservationId 기준으로 모든 주문 항목의 총 금액을 가져옴.
    @Query("SELECT COALESCE(SUM(soi.count * soi.orderPrice), 0) " +
            "FROM ServiceOrder so " +
            "JOIN so.serviceOrderItemList soi " +
            "WHERE so.reservation.reservationId = :reservationId")
    Integer getTotalOrderPriceByReservation(@Param("reservationId") Integer reservationId);
}
