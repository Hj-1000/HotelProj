package com.ntt.ntt.Controller.serviceMenu;

import com.ntt.ntt.DTO.ServiceMenuDTO;
import com.ntt.ntt.Service.ServiceMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Tag(name = "ServiceMenuRestController", description = "메뉴 레스트 컨트롤러")
public class ServiceMenuRestController {

    private final ServiceMenuService serviceMenuService;

    @Operation(summary = "유저 주문페이지 메뉴 목록", description = "유저의 주문페이지에 메뉴 목록을 불러온다.")
    @GetMapping
    public ResponseEntity<List<ServiceMenuDTO>> getMenusByCategory(@RequestParam Integer serviceCateId) {

        List<ServiceMenuDTO> menus = serviceMenuService.getMenusByCategory(serviceCateId);

        return ResponseEntity.ok(menus);
    }
}
