package com.ntt.ntt.Controller.ServiceMenu;

import com.ntt.ntt.Constant.ServiceMenuStatus;
import com.ntt.ntt.DTO.ServiceCateDTO;
import com.ntt.ntt.DTO.ServiceMenuDTO;
import com.ntt.ntt.Entity.ServiceMenu;
import com.ntt.ntt.Service.ServiceCateService;
import com.ntt.ntt.Service.ServiceMenuService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
@Tag(name = "ServiceMenuUserController", description = "유저가 보는 메뉴 목록창")
public class ServiceMenuUserController {
    private final ServiceMenuService serviceMenuService;
    private final ServiceCateService serviceCateService;
    private final PaginationUtil paginationUtil;

    @Operation(summary = "유저의 메뉴 목록", description = "등록되어 있는 메뉴를 조회한다.")
    @GetMapping("/myPage/menu/list")
    public String list(Model model, Principal principal) {
        if (principal == null || principal.getName() == null) {
            // 로그인을 하지 않으면 로그인 페이지로 이동
            return "redirect:/login";
        }

        List<ServiceMenuDTO> serviceMenuDTOS = serviceMenuService.listUserMenu();
        List<ServiceCateDTO> serviceCateDTOS = serviceCateService.getAllServiceCate();
        model.addAttribute("serviceCateDTOS", serviceCateDTOS);
        model.addAttribute("serviceMenuDTOS", serviceMenuDTOS);

        return "/myPage/menu/list";
    }

}
