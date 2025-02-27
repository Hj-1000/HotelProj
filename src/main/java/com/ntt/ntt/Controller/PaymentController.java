package com.ntt.ntt.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.DTO.PaymentDTO;
import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Service.MemberService;
import com.ntt.ntt.Service.PaymentService;
import com.ntt.ntt.Service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.*;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MemberService memberService;
    private final ReservationService reservationService;
    private final MemberRepository memberRepository;

    // 결제 정보를 저장
    @PostMapping("/payment")
    public ResponseEntity<String> registerProc(@RequestBody PaymentDTO paymentDTO) {
        try {
            paymentService.savePayment(paymentDTO);
            return ResponseEntity.ok("{\"success\": true, \"message\": \"결제 완료\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"success\": false, \"message\": \"결제 처리 중 오류가 발생했습니다.\"}");
        }
    }

    // 매출관리페이지
    @Operation(summary = "매출관리", description = "매출관리 페이지로 이동한다.")
    @GetMapping("/manager/sales")
    public String getSales(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(required = false) String roomName,
                           @RequestParam(required = false) String minPrice,
                           @RequestParam(required = false) String maxPrice,
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model, Pageable pageable) {
        try {
            if ("전체".equals(roomName)) roomName = null;

            // 필터링된 결제내역 리스트 가져오기
            List<PaymentDTO> filteredPayments = paymentService.getFilteredPayments(roomName, minPrice, maxPrice, startDate, endDate);

            // 전체 결제내역 리스트 가져오기 (필터링 적용 안됨)
            List<PaymentDTO> allPayments = paymentService.getAllPayments();

            // 페이징 처리
            int startIdx = page * size;
            int endIdx = Math.min(startIdx + size, filteredPayments.size());
            List<PaymentDTO> pagedPayments = filteredPayments.subList(startIdx, endIdx);

            Optional<Member> memberOptional = memberRepository.findByMemberEmail(userDetails.getUsername());
            if (memberOptional.isEmpty()) {
                return "redirect:/login";
            }

            Member member = memberOptional.get();

            // 예약 정보 가져오기
            Page<ReservationDTO> reservationsPage = reservationService.getUserReservations(member.getMemberId(), pageable);
            List<ReservationDTO> reservations = reservationsPage.getContent();

            // 결제 내역과 예약 내역을 묶어서 모델에 전달
            model.addAttribute("paymentDTOList", pagedPayments);
            model.addAttribute("reservationList", reservations);
            model.addAttribute("pageNumber", page);
            model.addAttribute("totalPages", (int) Math.ceil((double) filteredPayments.size() / size));
            model.addAttribute("size", size);
            model.addAttribute("allPayments", allPayments);  // 전체 결제 내역도 전달

            // JSON으로 변환할 데이터 리스트 생성
            List<Map<String, Object>> salesData = new ArrayList<>();
            for (PaymentDTO payment : allPayments) {
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
