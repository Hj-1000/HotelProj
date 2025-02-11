package com.ntt.ntt.Controller.ServiceMenu;

import com.ntt.ntt.DTO.ServiceMenuDTO;
import com.ntt.ntt.Service.ServiceMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class ServiceMenuRestController {
    private final ServiceMenuService serviceMenuService;

    @GetMapping
    public ResponseEntity<List<ServiceMenuDTO>> getMenusByCategory(@RequestParam Integer serviceCateId) {


        List<ServiceMenuDTO> menus = serviceMenuService.getMenusByCategory(serviceCateId);

        return ResponseEntity.ok(menus);
    }
}
