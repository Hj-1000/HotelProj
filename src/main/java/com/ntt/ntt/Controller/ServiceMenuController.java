package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.ServiceMenuDTO;
import com.ntt.ntt.Service.ServiceMenuService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/roomService/menu") //url roomService아래에
@Tag(name = "serviceMenuController", description = "룸서비스 메뉴 정보")
public class ServiceMenuController {
    private final ServiceMenuService serviceMenuService;
    private final PaginationUtil paginationUtil;

    @Operation(summary = "등록폼", description = "등록폼 페이지로 이동한다.")
    @GetMapping("/register")
    public String registerForm(Model model){
        //검증처리가 필요하면 빈 MenuDTO를 생성해서 전달한다.
        model.addAttribute("serviceMenuDTO", new ServiceMenuDTO());
        return "/manager/roomservice/menu/register";
    }

    @Operation(summary = "등록창", description = "데이터 등록 후 목록페이지로 이동한다.")
    @PostMapping("/register")
    public String registerProc(ServiceMenuDTO serviceMenuDTO, @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        log.info("post에서 등록할 serviceMenuDTO" + serviceMenuDTO);
        serviceMenuService.register(serviceMenuDTO, imageFiles);
        return "redirect:/roomService/menu/list";
    }

    @Operation(summary = "전체목록", description = "전체목록을 조회한다.")
    @GetMapping("/list")
    public String listSearch(@RequestParam(required = false) Integer serviceCateId,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String searchType,
                             @PageableDefault(page=1) Pageable page,
                             Model model) {
        // serviceCateId 가 존재하는 경우 해당 메뉴들만 조회
        Page<ServiceMenuDTO> serviceMenuDTOS = null;
        if (serviceCateId != null) {
            serviceMenuDTOS = serviceMenuService.list(page, keyword, searchType, serviceCateId);
        } else {
            serviceMenuDTOS = serviceMenuService.list(page, keyword, searchType, serviceCateId);
        }
        // 페이지 정보 계산
        Map<String, Integer> pageInfo = paginationUtil.pagination(serviceMenuDTOS);


        //만약 글이 10개 이하라면, 페이지 2는 표시되지 않도록 수정
        if (serviceMenuDTOS.getTotalPages() <= 1) {
            pageInfo.put("startPage", 1);
            pageInfo.put("endPage", 1);
        }
        model.addAttribute("serviceMenuDTOS", serviceMenuDTOS);
        model.addAttribute(pageInfo);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType",searchType);
        model.addAttribute("serviceCateId", serviceCateId);

        return "/manager/roomservice/menu/list";
    }

    @Operation(summary = "개별조회", description = "해당번호의 데이터를 조회한다.")
    @GetMapping("/read")
    public String read(Integer serviceMenuId, Model model) {
        try {
            ServiceMenuDTO serviceMenuDTO =
                    serviceMenuService.read(serviceMenuId);
            model.addAttribute("serviceMenuDTO", serviceMenuDTO);
            return "/manager/roomservice/menu/read";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "해당 카테고리를 찾을 수 없습니다");
            return "/manager/roomservice/menu/list";
        } catch (Exception e) {
            model.addAttribute("error", "서버 오류가 발생했습니다.");
            return "/manager/roomservice/menu/list";
        }
    }
    @Operation(summary = "수정폼", description = "해당 데이터를 조회 후 수정폼페이지로 이동한다.")
    @GetMapping("/update")
    public String updateForm(Integer serviceMenuId, Model model) {
        ServiceMenuDTO serviceMenuDTO =
                serviceMenuService.read(serviceMenuId);
        model.addAttribute("serviceMenuDTO",serviceMenuDTO);


        return "/manager/roomservice/menu/update";
    }

    @Operation(summary = "수정창", description = "수정할 내용을 데이터베이스에 저장 후 목록페이지로 이동한다.")
    @PostMapping("/update")
    public String updateProc(ServiceMenuDTO serviceMenuDTO, @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        serviceMenuService.update(serviceMenuDTO, imageFiles);
        return "redirect:/roomService/menu/read?serviceMenuId="+ serviceMenuDTO.getServiceMenuId();
    }

    @Operation(summary = "삭제처리", description = "해당 데이터를 삭제 후 목록페이지로 이동한다.")
    @GetMapping("/delete")
    public String deleteForm(Integer serviceMenuId) {

        serviceMenuService.delete(serviceMenuId);
        return "redirect:/roomService/menu/list";
    }
}
