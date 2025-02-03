package com.ntt.ntt.Controller;

import com.ntt.ntt.Constant.ServiceOrderStatus;
import com.ntt.ntt.DTO.ServiceOrderDTO;
import com.ntt.ntt.DTO.ServiceOrderItemDTO;
import com.ntt.ntt.Service.ServiceOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Log4j2
public class ServiceOrderRestController {

    private final ServiceOrderService serviceOrderService;

    // 특정 회원의 주문 목록 조회
    @GetMapping("/{memberId}")
    public ResponseEntity<List<ServiceOrderDTO>> getOrdersByMemberId(@PathVariable Integer memberId) {
        return ResponseEntity.ok(serviceOrderService.getOrdersByMemberId(memberId));
    }

    // 특정 주문 상세 조회
    @GetMapping("/details/{orderId}")
    public ResponseEntity<ServiceOrderDTO> getOrderDetails(@PathVariable Integer orderId) {
        return ResponseEntity.ok(serviceOrderService.getOrderDetails(orderId));
    }

    // 주문 생성
    @PostMapping("/{memberId}/{roomId}")
    public ResponseEntity<ServiceOrderDTO> createOrder(@PathVariable Integer memberId,
                                                       @PathVariable Integer roomId,
                                                       @RequestBody List<ServiceOrderItemDTO> orderItems) {
        return ResponseEntity.ok(serviceOrderService.createOrder(memberId, roomId, orderItems));
    }

    // 주문 상태 변경 (예: 결제 완료, 조리 중 등)
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ServiceOrderDTO> updateOrderStatus(@PathVariable Integer orderId,
                                                             @RequestParam ServiceOrderStatus status) {
        return ResponseEntity.ok(serviceOrderService.updateOrderStatus(orderId, status));
    }

    // 주문 취소
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Integer orderId) {
        serviceOrderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    // 모든 주문 목록 조회 (관리자 전용)
    @GetMapping("/admin/all")
    public ResponseEntity<List<ServiceOrderDTO>> getAllOrders() {
        return ResponseEntity.ok(serviceOrderService.getAllOrders());
    }
}
