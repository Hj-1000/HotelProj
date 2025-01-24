package com.ntt.ntt.Controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/roomService/menu") //url roomService아래에
@Tag(name = "serviceMenuController", description = "룸서비스 메뉴 정보")
public class ServiceMenuController {
    private final ServiceMenuService serviceMenuService;
    private final ServiceCateService serviceCateService;
    private final PaginationUtil paginationUtil;

    @Operation(summary = "등록폼", description = "등록폼 페이지로 이동한다.")
    @GetMapping("/register")
    public String registerForm(Model model){
        //검증처리가 필요하면 빈 MenuDTO를 생성해서 전달한다.
        List<ServiceCateDTO> serviceCateDTOS = serviceCateService.getAllServiceCate();
        model.addAttribute("serviceCateDTOS", serviceCateDTOS);
        model.addAttribute("serviceMenuDTO", new ServiceMenuDTO());
        return "/manager/roomservice/menu/register";
    }

    @Operation(summary = "등록창", description = "데이터 등록 후 목록페이지로 이동한다.")
    @PostMapping("/register")
    public String registerProc(ServiceMenuDTO serviceMenuDTO, @RequestParam("imageFiles") List<MultipartFile> imageFiles,
                               RedirectAttributes redirectAttributes) {
        log.info("post에서 등록할 serviceMenuDTO" + serviceMenuDTO);
        serviceMenuService.register(serviceMenuDTO, imageFiles);
        redirectAttributes.addFlashAttribute("message", "메뉴 등록이 완료되었습니다.");
        return "redirect:/roomService/menu/list";
    }



    @Operation(summary = "전체목록", description = "전체목록을 조회한다.")
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
        List<ServiceCateDTO> serviceCateDTOS = serviceCateService.getAllServiceCate();
        model.addAttribute("serviceCateDTOS", serviceCateDTOS);
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
        return "redirect:/roomService/menu/read?serviceMenuId="+ serviceMenuDTO.getServiceMenuId();
    }

    @Operation(summary = "삭제처리", description = "해당 데이터를 삭제 후 목록페이지로 이동한다.")
    @GetMapping("/delete")
    public String deleteForm(Integer serviceMenuId, RedirectAttributes redirectAttributes) {

        serviceMenuService.delete(serviceMenuId);
        redirectAttributes.addFlashAttribute("message", "메뉴 삭제가 완료되었습니다.");
        return "redirect:/roomService/menu/list";
    }

    //수정버튼 즉각반영을 위한 컨트롤러
    @PostMapping("/menu/updateStatus/{menuId}")
    @ResponseBody
    public ResponseEntity<?> updateMenuStatus(@PathVariable Integer menuId,
                                              @RequestBody Map<String, String> request) {
        try {
            String newStatus = request.get("serviceMenuStatus");
            serviceMenuService.updateMenuStatus(menuId, newStatus); // 상태 업데이트 서비스 호출
            return ResponseEntity.ok(Map.of("message", "메뉴 상태가 성공적으로 업데이트되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "상태 업데이트에 실패했습니다."));
        }
    }

}
