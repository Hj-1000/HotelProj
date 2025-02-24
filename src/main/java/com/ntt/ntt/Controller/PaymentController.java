package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.PaymentDTO;
import com.ntt.ntt.Service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

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
}
