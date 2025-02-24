package com.ntt.ntt.Controller.serviceOrder;

import com.ntt.ntt.DTO.ServiceOrderHistoryDTO;
import com.ntt.ntt.Service.ServiceOrderService;
import com.ntt.ntt.Util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Controller
@Log4j2
@RequiredArgsConstructor
public class ServiceOrderManagerController {
    private ServiceOrderService serviceOrderService;
    private final PaginationUtil paginationUtil;

    @GetMapping("/manager/order/list")
    public String orderListSearch(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String searchType,
                             @RequestParam(required = false) Integer reservationId,
                             @PageableDefault(page = 1) Pageable page, Model model) {

        Page<ServiceOrderHistoryDTO> serviceOrderHistoryDTOS =
                serviceOrderService.managerOrderList(page, keyword,searchType, reservationId);

        Map<String, Integer> pageInfo = paginationUtil.pagination(serviceOrderHistoryDTOS);

        if (serviceOrderHistoryDTOS.getTotalPages() <= 1) {
            pageInfo.put("startPage", 1);
            pageInfo.put("endPage", 1);
        }
        model.addAttribute("serviceOrderHistoryDTOS", serviceOrderHistoryDTOS);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("reservationId", reservationId);

        return "/manager/roomService/order/list";
    }

//    @GetMapping("/manager/order/list")
//    public String orderHistory(@PathVariable("page") Optional<Integer> page,
//                               Principal principal, Model model) {
//        if (principal == null) {
//            log.info("로그인을 하지않은 접속오류");
//            return "redirect:/login";
//        }
//
//        // 페이지 번호가 없으면 0을 기본값으로 설정
//        int currentPage = page.orElse(0);
//        Pageable pageable = PageRequest.of(currentPage, 4);
//        log.info(pageable.toString());
//
//        String memberEmail = principal.getName();
//
//        Page<ServiceOrderHistoryDTO> serviceOrderHistoryDTOPage =
//                serviceOrderService.getOrderList(memberEmail, pageable);
//
//        model.addAttribute("orders", serviceOrderHistoryDTOPage);
//        // HTML에서 getContent()를 호출할 것임
//        model.addAttribute("page", currentPage);
//        model.addAttribute("maxPage", 5);
//        return "/manager/roomService/order/list";
//    }
}
