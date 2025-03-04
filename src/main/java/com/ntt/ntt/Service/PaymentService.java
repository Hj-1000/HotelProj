package com.ntt.ntt.Service;

import com.ntt.ntt.Constant.ServiceOrderStatus;
import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.DTO.PaymentDTO;
import com.ntt.ntt.Entity.*;
import com.ntt.ntt.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final ModelMapper modelMapper;

    // 결제 정보 저장 메소드
    public Payment savePayment(PaymentDTO paymentDTO) {
        // 해당 ID로 Member, Room, Reservation 엔티티를 조회
        Member member = memberRepository.findById(paymentDTO.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Room room = roomRepository.findById(paymentDTO.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        Reservation reservation = reservationRepository.findById(paymentDTO.getReservationId())
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        //결제버튼 누르면 해당 서비스오더아이디를 찾아와서
        // 해당 예약에 포함된 PENDING 상태의 주문만 조회
        List<ServiceOrder> pendingOrders = serviceOrderRepository
                .findByReservationAndServiceOrderStatus(reservation, ServiceOrderStatus.PENDING);

        // 기존 결제 정보 조회 (reservationId로 조회)
        Payment existingPayment = paymentRepository.findByReservation(reservation);

        if (existingPayment != null) {
            // 기존 결제 정보가 있으면 새로운 값을 기존 값에 더함
            existingPayment.setRoomPrice(paymentDTO.getRoomPrice());
            existingPayment.setRoomServicePrice(paymentDTO.getRoomServicePrice());
            existingPayment.setTotalPrice(existingPayment.getTotalPrice() + paymentDTO.getTotalPrice()); // 기존값 + 새로운값
            existingPayment.setModDate(LocalDateTime.now());

            // PENDING 상태의 주문이 있을 경우 COMPLETED로 변경
            if (!pendingOrders.isEmpty()) {
                for (ServiceOrder order : pendingOrders) {
                    order.setServiceOrderStatus(ServiceOrderStatus.COMPLETED);
                    serviceOrderRepository.save(order);
                }
            }
            // 업데이트 후 저장
            return paymentRepository.save(existingPayment);

        } else {
            // 기존 결제 정보가 없으면 새로 결제 정보 저장
            Payment payment = new Payment();
            payment.setRoomPrice(paymentDTO.getRoomPrice());
            payment.setRoomServicePrice(paymentDTO.getRoomServicePrice());
            payment.setTotalPrice(paymentDTO.getTotalPrice());
            payment.setMember(member);
            payment.setRoom(room);
            payment.setReservation(reservation);
            payment.setRegDate(LocalDateTime.now());
            payment.setModDate(LocalDateTime.now());

            // PENDING 상태의 주문이 있을 경우 COMPLETED로 변경
            if (!pendingOrders.isEmpty()) {
                for (ServiceOrder order : pendingOrders) {
                    order.setServiceOrderStatus(ServiceOrderStatus.COMPLETED);
                    serviceOrderRepository.save(order);
                }
            }

            // 새로운 결제 정보 저장

            return paymentRepository.save(payment);
        }

    }

    // 결제 내역 조회 및 검색 기능
    public List<PaymentDTO> getFilteredPaymentsByRoomIds(List<Integer> roomIds, String hotelName, String roomName, String minPrice, String maxPrice, String startDate, String endDate) {
        List<Payment> payments = paymentRepository.findByRoomRoomIdIn(roomIds);

        if (hotelName != null && !hotelName.isEmpty()) {
            payments = payments.stream()
                    .filter(payment -> payment.getRoom().getHotelId().getHotelName().contains(hotelName))
                    .collect(Collectors.toList());
        }
        if (roomName != null && !roomName.isEmpty()) {
            payments = payments.stream()
                    .filter(payment -> payment.getRoom().getRoomName().contains(roomName))
                    .collect(Collectors.toList());
        }
        if (minPrice != null && !minPrice.isEmpty()) {
            try {
                int min = Integer.parseInt(minPrice);
                payments = payments.stream()
                        .filter(payment -> payment.getTotalPrice() >= min)
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                System.out.println("잘못된 최소 금액 입력: " + minPrice);
            }
        }
        if (maxPrice != null && !maxPrice.isEmpty()) {
            try {
                int max = Integer.parseInt(maxPrice);
                payments = payments.stream()
                        .filter(payment -> payment.getTotalPrice() <= max)
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                System.out.println("잘못된 최대 금액 입력: " + maxPrice);
            }
        }
        if (startDate != null && !startDate.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate start = LocalDate.parse(startDate, formatter);
                payments = payments.stream()
                        .filter(payment -> payment.getModDate().toLocalDate().isEqual(start) || payment.getModDate().toLocalDate().isAfter(start))
                        .collect(Collectors.toList());
            } catch (DateTimeParseException e) {
                System.out.println("잘못된 시작 날짜 입력: " + startDate);
            }
        }
        if (endDate != null && !endDate.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate end = LocalDate.parse(endDate, formatter);
                payments = payments.stream()
                        .filter(payment -> payment.getModDate().toLocalDate().isEqual(end) || payment.getModDate().toLocalDate().isBefore(end))
                        .collect(Collectors.toList());
            } catch (DateTimeParseException e) {
                System.out.println("잘못된 종료 날짜 입력: " + endDate);
            }
        }

        return payments.stream()
                .map(payment -> modelMapper.map(payment, PaymentDTO.class))
                .collect(Collectors.toList());
    }

    // 전체 결제 내역을 가져오는 메서드
    public List<PaymentDTO> getAllPayments() {
        // 전체 결제 내역을 DB에서 가져오는 로직
        List<Payment> allPayments = paymentRepository.findAll();

        // Payment 객체를 PaymentDTO로 변환하여 반환
        return allPayments.stream()
                .map(payment -> new PaymentDTO(
                        payment.getPaymentId(),           // paymentId
                        payment.getRoomPrice(),           // roomPrice
                        payment.getRoomServicePrice(),    // roomServicePrice
                        payment.getTotalPrice(),          // totalPrice
                        payment.getMember().getMemberId(),// memberId (Member에서 가져옴)
                        payment.getRoom().getRoomId(),    // roomId (Room에서 가져옴)
                        payment.getReservation().getReservationId(), // reservationId (Reservation에서 가져옴)
                        payment.getRegDate(),             // regDate
                        payment.getModDate()              // modDate
                ))
                .collect(Collectors.toList());
    }
}

