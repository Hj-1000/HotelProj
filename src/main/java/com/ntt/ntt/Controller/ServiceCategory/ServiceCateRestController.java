package com.ntt.ntt.Controller.ServiceCategory;

import com.ntt.ntt.DTO.ServiceCateDTO;
import com.ntt.ntt.Service.ServiceCateService;
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
public class ServiceCateRestController {
    private final ServiceCateService serviceCateService;

//    @GetMapping("/all")
//    public ResponseEntity<List<ServiceCateDTO>> getAllCategories() {
//        List<ServiceCateDTO> categories = serviceCateService.getAllServiceCate();
//        return ResponseEntity.ok(categories);
//    }

    @GetMapping
    public ResponseEntity<List<ServiceCateDTO>> getCategoriesByHotel(@RequestParam Integer hotelId) {
        List<ServiceCateDTO> categories = serviceCateService.listByHotel(hotelId);
        return ResponseEntity.ok(categories);
    }
}
