package com.ntt.ntt.Controller;

import com.ntt.ntt.Constant.ServiceOrderStatus;
import com.ntt.ntt.DTO.ServiceOrderDTO;
import com.ntt.ntt.DTO.ServiceOrderRequestDTO;
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
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    // 특정 회원의 주문 목록 조회
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<ServiceOrderDTO>> getOrdersByMemberId(@PathVariable Integer memberId) {
        List<ServiceOrderDTO> serviceOrderDTOs = serviceOrderService.getOrdersByMemberId(memberId);
        return ResponseEntity.ok(serviceOrderDTOs);
    }

    // 주문 생성
    @PostMapping
    public ResponseEntity<ServiceOrderDTO> createOrder(@RequestBody ServiceOrderRequestDTO serviceOrderRequestDTO) {
        ServiceOrderDTO serviceOrderDTO = serviceOrderService.createOrder(
                serviceOrderRequestDTO.getMemberId(),
                serviceOrderRequestDTO.getRoomId(),
                serviceOrderRequestDTO.getOrderItems()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderDTO);
    }

    // 주문 상태 변경
    @PutMapping("/{serviceOrderId}/status")
    public ResponseEntity<ServiceOrderDTO> updateOrderStatus(@PathVariable Integer serviceOrderId,
                                                             @RequestBody ServiceOrderStatus status) {
        ServiceOrderDTO serviceOrderDTO = serviceOrderService.updateOrderStatus(serviceOrderId, status);
        return ResponseEntity.ok(serviceOrderDTO);
    }
}
