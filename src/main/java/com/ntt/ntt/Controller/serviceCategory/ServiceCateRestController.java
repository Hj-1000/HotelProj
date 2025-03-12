package com.ntt.ntt.Controller.serviceCategory;

import com.ntt.ntt.DTO.ServiceCateDTO;
import com.ntt.ntt.Service.ServiceCateService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "ServiceCateRestController", description = "카테고리 레스트 컨트롤러")
public class ServiceCateRestController {
    private final ServiceCateService serviceCateService;

//    @GetMapping("/all")
//    public ResponseEntity<List<ServiceCateDTO>> getAllCategories() {
//        List<ServiceCateDTO> categories = serviceCateService.getAllServiceCate();
//        return ResponseEntity.ok(categories);
//    }

    @Operation(summary = "유저 주문페이지 카테고리 목록", description = "예약한 지사에 속하는 카테고리 목록을 불러온다.")
    @GetMapping
    public ResponseEntity<List<ServiceCateDTO>> getCategoriesByHotel(@RequestParam Integer hotelId) {
        List<ServiceCateDTO> categories = serviceCateService.listByHotel(hotelId);
        return ResponseEntity.ok(categories);
    }
}
