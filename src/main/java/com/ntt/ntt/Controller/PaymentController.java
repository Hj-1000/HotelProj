package com.ntt.ntt.Controller;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.DTO.PaymentDTO;
import com.ntt.ntt.Service.MemberService;
import com.ntt.ntt.Service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MemberService memberService;

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
    public String getSales(@RequestParam(required = false) String room,
                           @RequestParam(required = false) String minPrice,
                           @RequestParam(required = false) String maxPrice,
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {
        try {
            if ("전체".equals(room)) room = null;

            // 필터링된 결제내역 리스트 가져오기
            List<PaymentDTO> filteredPayments = paymentService.getFilteredPayments(room, minPrice, maxPrice, startDate, endDate);

            // memberId 기준 내림차순 정렬
            // filteredMembers.sort(Comparator.comparing(MemberDTO::getMemberId).reversed());

            // 페이징 처리
            int startIdx = page * size;
            int endIdx = Math.min(startIdx + size, filteredPayments.size());
            List<PaymentDTO> pagedPayments = filteredPayments.subList(startIdx, endIdx);

            model.addAttribute("paymentDTOList", pagedPayments);
            model.addAttribute("pageNumber", page);
            model.addAttribute("totalPages", (int) Math.ceil((double) filteredPayments.size() / size));
            model.addAttribute("size", size);

        } catch (Exception e) {
            model.addAttribute("error", "회원 목록을 가져오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        return "manager/sales";
    }
}
