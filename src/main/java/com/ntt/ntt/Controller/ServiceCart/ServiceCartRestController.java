package com.ntt.ntt.Controller.ServiceCart;

import com.ntt.ntt.DTO.ServiceCartDTO;
import com.ntt.ntt.DTO.ServiceCartItemDTO;
import com.ntt.ntt.Service.ServiceCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Log4j2
public class ServiceCartRestController {

    private final ServiceCartService serviceCartService;

    // 특정 회원의 장바구니 조회
    @GetMapping("/{memberId}")
    public ResponseEntity<ServiceCartDTO> getCartByMemberId(@PathVariable Integer memberId) {
        return ResponseEntity.ok(serviceCartService.getCartByMemberId(memberId));
    }

    // 장바구니에 아이템 추가
    @PostMapping("/{serviceCartId}/items")
    public ResponseEntity<Void> addItemToCart(@PathVariable Integer serviceCartId, @RequestBody ServiceCartItemDTO itemDTO) {
        serviceCartService.addItemToCart(itemDTO.getServiceMenuId(), itemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 장바구니 아이템 수량 변경
    @PutMapping("/items/{serviceCartItemId}")
    public ResponseEntity<Void> updateCartItemQuantity(@PathVariable Integer serviceCartItemId, @RequestParam Integer newCount) {
        serviceCartService.updateCartItemQuantity(serviceCartItemId, newCount);
        return ResponseEntity.ok().build();
    }

    // 장바구니 아이템 삭제
    @DeleteMapping("/items/{serviceCartItemId}")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Integer serviceCartItemId) {
        serviceCartService.removeItemFromCart(serviceCartItemId);
        return ResponseEntity.noContent().build();
    }

    // 장바구니 전체 비우기
    @DeleteMapping("/{memberId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Integer memberId) {
        serviceCartService.clearCart(memberId);
        return ResponseEntity.noContent().build();
    }
}
