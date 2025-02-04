package com.ntt.ntt.Controller.ServiceOrder;

import com.ntt.ntt.Constant.ServiceOrderStatus;
import com.ntt.ntt.DTO.ServiceOrderDTO;
import com.ntt.ntt.DTO.ServiceOrderItemDTO;
import com.ntt.ntt.Service.ServiceOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
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

//    // 주문 생성
//    @PostMapping("/{memberId}/{roomId}")
//    public ResponseEntity<ServiceOrderDTO> createOrder(@PathVariable Integer memberId,
//                                                       @PathVariable Integer roomId,
//                                                       @RequestBody List<ServiceOrderItemDTO> orderItems) {
//        return ResponseEntity.ok(serviceOrderService.createOrder(memberId, roomId, orderItems));
//    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody List<ServiceOrderItemDTO> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return ResponseEntity.badRequest().body("주문 항목이 비어 있습니다.");
        }

        Integer memberId = 1;  // 실제 로그인한 사용자 ID 필요
        Integer roomId = 1;  // 실제 사용자 방 번호 필요

        try {
            ServiceOrderDTO order = serviceOrderService.createOrder(memberId, roomId, orderItems);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생: " + e.getMessage());
        }
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
