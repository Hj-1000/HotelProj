package com.ntt.ntt.Controller.serviceMenu;

import com.ntt.ntt.Constant.ServiceMenuStatus;
import com.ntt.ntt.DTO.ServiceCateDTO;
import com.ntt.ntt.DTO.ServiceMenuDTO;
import com.ntt.ntt.Service.ServiceCateService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("manager/roomService/menu") //url roomService아래에
@Tag(name = "serviceMenuManagerController", description = "룸서비스 메뉴 정보")
public class ServiceMenuManagerController {
    private final ServiceMenuService serviceMenuService;
    private final ServiceCateService serviceCateService;
    private final PaginationUtil paginationUtil;

    @Operation(summary = "등록폼", description = "등록폼 페이지로 이동한다.")
    @GetMapping("/register")
    public String registerForm(@RequestParam(required = false) Integer serviceCateId, Model model){
        //검증처리가 필요하면 빈 MenuDTO를 생성해서 전달한다.
        List<ServiceCateDTO> serviceCateDTOS = serviceCateService.getAllServiceCate();
        model.addAttribute("serviceCateDTOS", serviceCateDTOS);
        model.addAttribute("serviceMenuDTO", new ServiceMenuDTO());
        model.addAttribute("selectServiceCateId", serviceCateId);
        return "/manager/roomservice/menu/register";
    }

    @Operation(summary = "등록창", description = "데이터 등록 후 목록페이지로 이동한다.")
    @PostMapping("/register")
    public String registerProc(ServiceMenuDTO serviceMenuDTO, @RequestParam("imageFiles") List<MultipartFile> imageFiles,
                               RedirectAttributes redirectAttributes) {
        log.info("post에서 등록할 serviceMenuDTO" + serviceMenuDTO);
        serviceMenuService.register(serviceMenuDTO, imageFiles);
        redirectAttributes.addFlashAttribute("message", "메뉴 등록이 완료되었습니다.");

        //등록된 메뉴의 카테고리Id 가져오기
        Integer serviceCateId = serviceMenuDTO.getServiceCateId().getServiceCateId();
        return "redirect:/manager/roomService/menu/list?serviceCateId=" + serviceCateId;
    }



//    @Operation(summary = "전체목록", description = "전체목록을 조회한다.")
//    @GetMapping("/list")
//    public String list(@RequestParam(required = false) Integer serviceCateId,
//                       @RequestParam(required = false) Integer hotelId,
//                       @RequestParam(required = false) String keyword,
//                       @RequestParam(required = false) String searchType,
//                       @RequestParam(required = false) String status, // 상태값 추가
//                       @PageableDefault(page = 1) Pageable page,
//                       Model model) {
//
//        // 메뉴 목록 조회
//        Page<ServiceMenuDTO> serviceMenuDTOS;
//        if (serviceCateId != null) {
//            // 카테고리 ID가 있을 경우
//            serviceMenuDTOS = serviceMenuService.list(page, keyword, searchType, serviceCateId, status);
//        } else {
//            // 카테고리 ID가 없을 경우 (전체 조회)
//            serviceMenuDTOS = serviceMenuService.list(page, keyword, searchType, null, status);
//        }
//
//        // 페이지 정보 계산
//        Map<String, Integer> pageInfo = paginationUtil.pagination(serviceMenuDTOS);
//
//        // 페이지가 하나뿐일 경우, startPage와 endPage를 1로 설정
//        if (serviceMenuDTOS.getTotalPages() <= 1) {
//            pageInfo.put("startPage", 1);
//            pageInfo.put("endPage", 1);
//        }
//
//
//
//        // 모든 카테고리와 상태값 추가
//        Page<ServiceCateDTO> serviceCateDTOS = serviceCateService.list(page, keyword, searchType, hotelId);
//
//        model.addAttribute("serviceCateDTOS", serviceCateDTOS);
//        model.addAttribute("serviceMenuDTOS", serviceMenuDTOS);
//        model.addAttribute("serviceMenuStatus", ServiceMenuStatus.values()); // 상태값 enum 전달
//        model.addAttribute("pageInfo", pageInfo);
//
//        // 검색 관련 정보 전달
//        model.addAttribute("keyword", keyword);
//        model.addAttribute("searchType", searchType);
//        model.addAttribute("status", status); // 선택한 상태값 전달
//        model.addAttribute("serviceCateId", serviceCateId);
//
//        return "/manager/roomservice/menu/list";
//    }

    @Operation(summary = "특정지사의 서비스 메뉴", description = "특정 지사에 등록된 카테고리의 메뉴 목록을 조회한다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @GetMapping("/list")
    public String list(@RequestParam(required = false) Integer serviceCateId,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String status, // 상태값 추가
                       @PageableDefault(page = 1) Pageable page,
                       Model model) {

        // 메뉴 목록 조회
        Page<ServiceMenuDTO> serviceMenuDTOS;
        if (serviceCateId != null) {
            // 카테고리 ID가 있을 경우
            serviceMenuDTOS = serviceMenuService.list(page, keyword, searchType, serviceCateId, status);
        } else {
            // 카테고리 ID가 없을 경우 (전체 조회)
            serviceMenuDTOS = serviceMenuService.list(page, keyword, searchType, null, status);
        }

        // 페이지 정보 계산
        Map<String, Integer> pageInfo = paginationUtil.pagination(serviceMenuDTOS);

        // 페이지가 하나뿐일 경우, startPage와 endPage를 1로 설정
        if (serviceMenuDTOS.getTotalPages() <= 1) {
            pageInfo.put("startPage", 1);
            pageInfo.put("endPage", 1);
        }

        // 모든 카테고리와 상태값 추가
        if (serviceCateId != null) {
            ServiceCateDTO serviceCateDTO = serviceCateService.read(serviceCateId);
            Integer hotelId = serviceCateDTO.getHotelId().getHotelId();
            List<ServiceCateDTO> serviceCateDTOS = serviceCateService.listByHotel(hotelId);
            model.addAttribute("serviceCateDTOS", serviceCateDTOS);
        }else {
            List<ServiceCateDTO> serviceCateDTOS = serviceCateService.getAllServiceCate();
            model.addAttribute("serviceCateDTOS", serviceCateDTOS);
        }




        model.addAttribute("serviceMenuDTOS", serviceMenuDTOS);
        model.addAttribute("serviceMenuStatus", ServiceMenuStatus.values()); // 상태값 enum 전달
        model.addAttribute("pageInfo", pageInfo);

        // 검색 관련 정보 전달
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("status", status); // 선택한 상태값 전달
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
    public String updateProc(ServiceMenuDTO serviceMenuDTO, @RequestParam("imageFiles") List<MultipartFile> imageFiles,
                             RedirectAttributes redirectAttributes) {
        serviceMenuService.update(serviceMenuDTO, imageFiles);
        redirectAttributes.addFlashAttribute("message", "메뉴 수정이 완료되었습니다.");
        return "redirect:/manager/roomService/menu/read?serviceMenuId="+ serviceMenuDTO.getServiceMenuId();
    }

    @Operation(summary = "삭제처리", description = "해당 데이터를 삭제 후 목록페이지로 이동한다.")
    @GetMapping("/delete")
    public String deleteForm(Integer serviceMenuId, RedirectAttributes redirectAttributes) {

        //삭제 전 메뉴 정보 조회
        ServiceMenuDTO serviceMenuDTO = serviceMenuService.read(serviceMenuId);

        serviceMenuService.delete(serviceMenuId);
        redirectAttributes.addFlashAttribute("message", "메뉴 삭제가 완료되었습니다.");

        Integer serviceCateId = serviceMenuDTO.getServiceCateId().getServiceCateId();
        return "redirect:/manager/roomService/menu/list?serviceCateId=" + serviceCateId;
    }
}
