package com.ntt.ntt.Controller.ServiceMenu;

import com.ntt.ntt.Constant.ServiceMenuStatus;
import com.ntt.ntt.DTO.ServiceCateDTO;
import com.ntt.ntt.DTO.ServiceMenuDTO;
import com.ntt.ntt.Service.ServiceCateService;
import com.ntt.ntt.Service.ServiceMenuService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ServiceMenuUserController {
//    private final ServiceMenuService serviceMenuService;
//    private final ServiceCateService serviceCateService;
//    private final PaginationUtil paginationUtil;

//    @Operation(summary = "유저의 메뉴 목록", description = "등록되어 있는 메뉴를 조회한다.")
//    @GetMapping("/myPage/menu/list")
//    public String list(@RequestParam(required = false) Integer serviceCateId,
//                       @RequestParam(required = false) String keyword,
//                       @RequestParam(required = false) String searchType,
//                       @RequestParam(required = false) String status,
//                       @PageableDefault(page = 1) Pageable page,
//                       Model model) {
//
//        Page<ServiceMenuDTO> serviceMenuDTOS;
//        if (serviceCateId != null) {
//            serviceMenuDTOS = serviceMenuService.list(page, keyword, searchType, serviceCateId, status);
//        } else {
//            serviceMenuDTOS = serviceMenuService.list(page, keyword, searchType, null, status);
//        }
//
//        Map<String, Integer> pageInfo = paginationUtil.pagination(serviceMenuDTOS);
//        if (serviceMenuDTOS.getTotalPages() <= 1) {
//            pageInfo.put("startPage", 1);
//            pageInfo.put("endPage", 1);
//        }
//
//        List<ServiceCateDTO> serviceCateDTOS = serviceCateService.getAllServiceCate();
//        model.addAttribute("serviceCateDTOS", serviceCateDTOS);
//        model.addAttribute("serviceMenuDTOS", serviceMenuDTOS);
//        model.addAttribute("serviceMenuStatus", ServiceMenuStatus.values());
//        model.addAttribute("pageInfo", pageInfo);
//        model.addAttribute("keyword", keyword);
//        model.addAttribute("searchType", searchType);
//        model.addAttribute("status", status);
//        model.addAttribute("serviceCateId", serviceCateId);
//
//        return "/myPage/menu/list";
//    }
//
//    @ResponseBody
////    @GetMapping("/api/user/menus")
////    public ResponseEntity<List<ServiceMenuDTO>> getMenusByCategory(@RequestParam Integer serviceCateId) {
////        List<ServiceMenuDTO> menus = serviceMenuService.getMenusByCategory(serviceCateId);
////        return ResponseEntity.ok(menus);
////    }
//
//    @GetMapping("/api/user/menus")
//    public List<ServiceMenuDTO> getMenus(@RequestParam Integer serviceCateId) {
//        return serviceMenuService.getMenusByCategory(serviceCateId); // JSON으로 자동 변환되어 반환됨
//    }
}
