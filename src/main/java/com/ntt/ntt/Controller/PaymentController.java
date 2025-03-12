package com.ntt.ntt.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntt.ntt.DTO.*;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.RoomRepository;
import com.ntt.ntt.Repository.company.CompanyRepository;
import com.ntt.ntt.Repository.hotel.HotelRepository;
import com.ntt.ntt.Service.PaymentService;
import com.ntt.ntt.Service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Tag(name = "paymentController", description = "결제, 매출 관리 컨트롤러")
public class PaymentController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;
    private final MemberRepository memberRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final CompanyRepository companyRepository;

    // 결제 정보를 저장
    @PostMapping("/payment")
    @Operation(summary = "결제 처리", description = "결제 정보를 데이터에 저장한다.")
    public ResponseEntity<String> registerProc(@RequestBody PaymentDTO paymentDTO) {
        try {
            paymentService.savePayment(paymentDTO);
            return ResponseEntity.ok("{\"success\": true, \"message\": \"결제 완료\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"success\": false, \"message\": \"결제 처리 중 오류가 발생했습니다.\"}");
        }
    }

    // 매출관리페이지
    @Operation(summary = "지사 매출관리", description = "매출관리 페이지로 이동한다.")
    @GetMapping("/manager/sales")
    public String getSales(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(required = false) String hotelName,
                           @RequestParam(required = false) String roomName,
                           @RequestParam(required = false) String minPrice,
                           @RequestParam(required = false) String maxPrice,
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model, Pageable pageable, Principal principal) {
        try {

            // 로그인한 사용자의 이메일 가져오기
            String userEmail = principal.getName();
            Member member = memberRepository.findByMemberEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

            // 로그인한 사용자의 역할 확인
            boolean isManager = userDetails.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MANAGER"));
            boolean isChief = userDetails.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_CHIEF"));
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            List<Integer> roomIds;

            if (isManager) {
                // MANAGER 인 경우 로그인한 사용자가 관리하는 호텔 ID 목록 가져오기
                List<Integer> hotelIds = hotelRepository.findHotelIdsByMemberId(member.getMemberId());

                // 해당 호텔들에 속한 roomId 목록 가져오기
                roomIds = roomRepository.findRoomIdsByHotelIds(hotelIds);

            } else if (isChief) {
                // CHIEF 인 경우 로그인한 사용자가 관리하는 본사 ID 목록 가져오기
                List<Integer> companyIds = companyRepository.findCompanyIdsByMemberId(member.getMemberId());

                // 해당 본사에 속한 호텔 ID 목록 가져오기
                List<Integer> hotelIds = hotelRepository.findHotelIdsByCompanyIds(companyIds);

                // 해당 호텔들에 속한 roomId 목록 가져오기
                roomIds = roomRepository.findRoomIdsByHotelIds(hotelIds);

            } else if (isAdmin) {
                // ADMIN 인 경우 모든 방 목록 가져오기
                List<Room> allRooms = roomRepository.findAll();

                // Room ID만 추출하여 List<Integer>로 변환
                roomIds = allRooms.stream()
                        .map(Room::getRoomId) // Room 객체에서 roomId 추출
                        .collect(Collectors.toList());

            } else {
                // USER 인 경우(MANAGER, CHIEF, ADMIN 모두 아닌 경우) 예외 처리
                throw new RuntimeException("권한이 없습니다.");
            }

            // 필터링된 결제내역 리스트 가져오기
            List<PaymentDTO> filteredPayments = paymentService.getFilteredPaymentsByRoomIds(roomIds, hotelName, roomName, minPrice, maxPrice, startDate, endDate);

            // 페이징 처리
            int startIdx = page * size;
            int endIdx = Math.min(startIdx + size, filteredPayments.size());

            Optional<Member> memberOptional = memberRepository.findByMemberEmail(userDetails.getUsername());
            if (memberOptional.isEmpty()) {
                return "redirect:/login";
            }

            // 예약 정보 가져오기
            Page<ReservationDTO> reservationsPage = reservationService.getAllReservations(pageable);
            List<ReservationDTO> reservations = reservationsPage.getContent();

            // 필터링된 결제 내역 리스트를 ReservationId 기준으로 정렬
            filteredPayments.sort(Comparator.comparing(PaymentDTO::getReservationId));

            // 정렬 후 페이징 처리
            List<PaymentDTO> pagedPayments = new ArrayList<>(filteredPayments.subList(startIdx, endIdx));

            // 예약 내역 필터링 후 정렬
            List<ReservationDTO> filteredReservations = reservations.stream()
                    .filter(reservation -> filteredPayments.stream()
                            .anyMatch(payment -> payment.getReservationId().equals(reservation.getReservationId())))
                    .sorted(Comparator.comparing(ReservationDTO::getReservationId))
                    .collect(Collectors.toList());

            // 결제 내역과 예약 내역을 묶어서 모델에 전달
            model.addAttribute("paymentDTOList", pagedPayments);
            model.addAttribute("reservationList", filteredReservations);
            model.addAttribute("pageNumber", page);
            model.addAttribute("totalPages", (int) Math.ceil((double) filteredPayments.size() / size));
            model.addAttribute("size", size);
            model.addAttribute("filteredPayments", pagedPayments);  // 전체 결제 내역도 전달

            // JSON으로 변환할 데이터 리스트 생성
            List<Map<String, Object>> salesData = new ArrayList<>();
            for (PaymentDTO payment : filteredPayments) {
                Map<String, Object> data = new HashMap<>();
                data.put("date", payment.getModDate().toString()); // 날짜
                data.put("totalSales", payment.getTotalPrice());   // 결제 완료 금액
                data.put("roomSales", payment.getRoomPrice());   // 객실 금액
                data.put("serviceSales", payment.getRoomServicePrice());   // 룸서비스 금액
                salesData.add(data);
            }

            // JSON 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String salesDataJson = objectMapper.writeValueAsString(salesData);
            model.addAttribute("salesDataJson", salesDataJson);

        } catch (Exception e) {
            model.addAttribute("error", "회원 목록을 가져오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        return "manager/sales";
    }
}
