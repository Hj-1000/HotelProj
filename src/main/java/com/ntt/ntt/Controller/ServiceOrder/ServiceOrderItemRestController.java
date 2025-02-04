package com.ntt.ntt.Controller.ServiceOrder;

import com.ntt.ntt.DTO.ServiceOrderItemDTO;
import com.ntt.ntt.Service.ServiceOrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
@Log4j2
public class ServiceOrderItemRestController {

    private final ServiceOrderItemService serviceOrderItemService;

    // 특정 주문의 아이템 목록 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<List<ServiceOrderItemDTO>> getItemsByOrderId(@PathVariable Integer orderId) {
        return ResponseEntity.ok(serviceOrderItemService.getItemsByOrderId(orderId));
    }

    // 개별 주문 아이템 추가
    @PostMapping("/{orderId}")
    public ResponseEntity<ServiceOrderItemDTO> addOrderItem(@PathVariable Integer orderId, @RequestBody ServiceOrderItemDTO itemDTO) {
        return ResponseEntity.ok(serviceOrderItemService.addOrderItem(orderId, itemDTO));
    }

    // 주문 아이템 수량 변경
    @PutMapping("/{orderItemId}")
    public ResponseEntity<ServiceOrderItemDTO> updateOrderItemCount(@PathVariable Integer orderItemId, @RequestParam Integer newCount) {
        return ResponseEntity.ok(serviceOrderItemService.updateOrderItemCount(orderItemId, newCount));
    }

    // 주문 아이템 삭제
    @DeleteMapping("/{orderItemId}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Integer orderItemId) {
        serviceOrderItemService.deleteOrderItem(orderItemId);
        return ResponseEntity.noContent().build();
    }
}
