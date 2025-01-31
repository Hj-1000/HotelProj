package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.ServiceOrderItemDTO;
import com.ntt.ntt.Service.ServiceOrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
@Log4j2
public class ServiceOrderItemController {

    private final ServiceOrderItemService serviceOrderItemService;

    // 특정 주문의 아이템 목록 조회
    @GetMapping("/order/{serviceOrderId}")
    public ResponseEntity<List<ServiceOrderItemDTO>> getItemsByOrderId(@PathVariable Integer serviceOrderId) {
        List<ServiceOrderItemDTO> serviceOrderItemDTOs = serviceOrderItemService.getItemsByOrderId(serviceOrderId);
        return ResponseEntity.ok(serviceOrderItemDTOs);
    }
}
