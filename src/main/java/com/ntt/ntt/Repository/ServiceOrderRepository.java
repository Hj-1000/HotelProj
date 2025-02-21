package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.ServiceOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Integer> {

    //특정 회원의 구매 이력으로 모두 불러오기
    @Query("select so from ServiceOrder so where so.member.memberEmail = :memberEmail order by so.regDate asc")
    public List<ServiceOrder> findServiceOrders(@Param("memberEmail") String memberEmail, Pageable pageable);

    //특정 회원의 주문갯수 검색 메서드
    @Query("select count(so) from ServiceOrder so where so.member.memberEmail = :memberEmail")
    public Integer totalCount(@Param("memberEmail") String memberEmail);

    public List<ServiceOrder> findByMember_MemberIdOrderByRegDateDesc(String memberEmail, Pageable pageable);

    //예약페이지에 룸서비스 주문금액을 추가하기 위해 쿼리문 추가 2025-02-21
    //이 메서드는 roomId를 기준으로 모든 주문 항목의 총 금액을 가져옴.
    @Query("SELECT COALESCE(SUM(soi.count * soi.orderPrice), 0) " +
            "FROM ServiceOrder so " +
            "JOIN so.serviceOrderItemList soi " +
            "WHERE so.reservation.reservationId = :reservationId")
    Integer getTotalOrderPriceByReservation(@Param("reservationId") Integer reservationId);
}
