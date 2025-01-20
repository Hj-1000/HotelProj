package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.ServiceCateDTO;
import com.ntt.ntt.Service.ServiceCateService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/roomService") //url roomService아래에
@Tag(name = "ServiceCateController", description = "룸서비스 카테고리 정보")
public class ServiceCateController {
    private final ServiceCateService serviceCateService;
    private final PaginationUtil paginationUtil;

    @Operation(summary = "등록폼", description = "등록폼 페이지로 이동한다.")
    @GetMapping("/register")
    public String registerForm(Model model){
        //검증처리가 필요하면 빈 CateDTO를 생성해서 전달한다.
        model.addAttribute("serviceCateDTO", new ServiceCateDTO());
        return "/manager/roomservice/category/register";
    }

    @Operation(summary = "등록창", description = "데이터 등록후 목록으로 이동한다.")
    @PostMapping("/register")
    public String registerProc(ServiceCateDTO serviceCateDTO, @RequestParam("imageFile") List<MultipartFile> imageFile) {
        log.info("post에서 등록할 serviceCateDTO" + serviceCateDTO);
        serviceCateService.register(serviceCateDTO, imageFile);
        return "redirect:/roomservice/list";
    }
}
