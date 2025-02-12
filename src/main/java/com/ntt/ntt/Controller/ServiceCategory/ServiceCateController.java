package com.ntt.ntt.Controller.ServiceCategory;

import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.ServiceCateDTO;
import com.ntt.ntt.Service.ServiceCateService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/roomService/category") //url roomService아래에
@Tag(name = "ServiceCateController", description = "룸서비스 카테고리 정보")
public class ServiceCateController {
    private final ServiceCateService serviceCateService;
    private final PaginationUtil paginationUtil;

    @Operation(summary = "등록폼", description = "등록폼 페이지로 이동한다.")
    @GetMapping("/register")
    public String registerForm(Model model){
        //검증처리가 필요하면 빈 CateDTO를 생성해서 전달한다.
        model.addAttribute("serviceCateDTO", new ServiceCateDTO());

        //hotelDTO hotelName 전달하기
        List<HotelDTO> hotelDTOS = serviceCateService.getAllHotel();
        model.addAttribute("hotelDTOS", hotelDTOS);
        model.addAttribute("hotelDTO", new HotelDTO());

        return "/manager/roomservice/category/register";
    }

    @Operation(summary = "등록창", description = "데이터 등록 후 목록페이지로 이동한다.")
    @PostMapping("/register")
    public String registerProc(ServiceCateDTO serviceCateDTO,
                               @RequestParam("imageFiles") List<MultipartFile> imageFiles,
                               RedirectAttributes redirectAttributes) {
        log.info("post에서 등록할 serviceCateDTO" + serviceCateDTO);
        serviceCateService.register(serviceCateDTO, imageFiles);

        // 등록된 지사의 hotelId 가져오기
        Integer hotelId = serviceCateDTO.getHotelId().getHotelId();  // 2025-02-11 추가

        redirectAttributes.addFlashAttribute("message", "카테고리 등록이 완료되었습니다.");
        return "redirect:/roomService/category/list?hotelId=" + hotelId;  // hotelId를 쿼리 파라미터로 전달 2025-02-11 추가
    }

    @Operation(summary = "전체목록", description = "전체목록을 조회한다.")
    @GetMapping("/list")
    public String listSearch(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String searchType,
                             @RequestParam(required = false) Integer hotelId, // hotelId 2024-02-11 추가
                             @PageableDefault(page = 1) Pageable page, Model model) {

        Page<ServiceCateDTO> serviceCateDTOS = serviceCateService.list(page, keyword, searchType, hotelId); // hotelId 전달 2024-02-11 추가

        // 페이지 정보 계산
        Map<String, Integer> pageInfo = paginationUtil.pagination(serviceCateDTOS);

        // 만약 글이 10개 이하라면, 페이지 2는 표시되지 않도록 수정
        if (serviceCateDTOS.getTotalPages() <= 1) {
            pageInfo.put("startPage", 1);
            pageInfo.put("endPage", 1);
        }

        // 모델에 데이터 추가
        model.addAttribute("serviceCateDTOS", serviceCateDTOS);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("hotelId", hotelId); // 2024-02-11 추가

        return "/manager/roomservice/category/list";
    }


    @Operation(summary = "개별조회", description = "해당번호의 데이터를 조회한다.")
    @GetMapping("/read")
    public String read(Integer serviceCateId, Model model) {
        try {
            ServiceCateDTO serviceCateDTO =
                    serviceCateService.read(serviceCateId);
            model.addAttribute("serviceCateDTO", serviceCateDTO);
            return "/manager/roomservice/category/read";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "해당 카테고리를 찾을 수 없습니다");
            return "/manager/roomservice/category/list";
        } catch (Exception e) {
            model.addAttribute("error", "서버 오류가 발생했습니다.");
            return "/manager/roomservice/category/list";
        }
    }
    @Operation(summary = "수정폼", description = "해당 데이터를 조회 후 수정폼페이지로 이동한다.")
    @GetMapping("/update")
    public String updateForm(Integer serviceCateId, Model model) {
        ServiceCateDTO serviceCateDTO =
                serviceCateService.read(serviceCateId);
        model.addAttribute("serviceCateDTO",serviceCateDTO);


        return "/manager/roomservice/category/update";
    }

    @Operation(summary = "수정창", description = "수정할 내용을 데이터베이스에 저장 후 목록페이지로 이동한다.")
    @PostMapping("/update")
    public String updateProc(ServiceCateDTO serviceCateDTO, @RequestParam("imageFiles") List<MultipartFile> imageFiles,
                             RedirectAttributes redirectAttributes) {
        serviceCateService.update(serviceCateDTO, imageFiles);
        redirectAttributes.addFlashAttribute("message", "카테고리 수정이 완료되었습니다.");
        return "redirect:/roomService/category/read?serviceCateId="+ serviceCateDTO.getServiceCateId();
    }

    @Operation(summary = "삭제처리", description = "해당 데이터를 삭제 후 목록페이지로 이동한다.")
    @GetMapping("/delete")
    public String deleteForm(Integer serviceCateId, RedirectAttributes redirectAttributes) {

        serviceCateService.delete(serviceCateId);
        redirectAttributes.addFlashAttribute("message", "카테고리 삭제가 완료되었습니다.");

        return "redirect:/roomService/category/list";
    }
}
