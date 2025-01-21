package com.ntt.ntt.Controller.company;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Service.ImageService;
import com.ntt.ntt.Service.company.CompanyService;
import com.ntt.ntt.Util.PaginationUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/company")
@AllArgsConstructor
@Log4j2
public class CompanyController {

    private final CompanyService companyService;
    private final PaginationUtil paginationUtil;

    private final ImageService imageService;

    //등록폼
    @GetMapping("/register")
    public String registerForm() {
        return "/company/register";
    }
    //등록처리
    @PostMapping("/register")
    public String registerProc(@ModelAttribute CompanyDTO companyDTO, List<MultipartFile> imageFiles) {
        log.info("본사 등록 진입");

        companyService.register(companyDTO, imageFiles);

        return "redirect:/company/list";
    }

    //목록
    @GetMapping("/list")
    public String list(@PageableDefault(page=1)Pageable page, Model model) {
        Page<CompanyDTO> companyDTOS = companyService.list(page);
        Map<String, Integer> pageInfo = paginationUtil.pagination(companyDTOS);
        model.addAttribute("companyDTOS", companyDTOS);
        model.addAttribute(pageInfo);
        return "/company/list";
    }

    //읽기
    @GetMapping("/read")
    public String read(@RequestParam Integer companyId, Model model) {
        try {
            // 서비스에서 CompanyDTO 객체를 받아옴
            CompanyDTO companyDTO = companyService.read(companyId);

            // companyDTO를 모델에 추가
            model.addAttribute("companyDTO", companyDTO);

            // "read" 뷰로 이동
            return "/company/read";

        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "해당 회사 정보를 찾을 수 없습니다.");
            return "/company/list";  // 회사 정보가 없을 경우 목록으로 이동
        } catch (Exception e) {
            model.addAttribute("error", "서버 오류가 발생했습니다.");
            return "/company/list";  // 기타 예외 처리
        }
    }

    //수정폼
    @GetMapping("/modify")
    public String modifyServiceHTML(Integer companyId, Model model) {
        CompanyDTO companyDTO = companyService.read(companyId);
        model.addAttribute("companyDTO", companyDTO);
        return "/company/modify";
    }
    //수정처리
    @PostMapping("/modify")
    public String modifyService(CompanyDTO companyDTO, List<MultipartFile> newImageFiles) {
        companyService.update(companyDTO, newImageFiles);
        return "redirect:/company/list";
    }

    //삭제
    @GetMapping("/delete")
    public String delete(Integer companyId) {
        companyService.delete(companyId);
        return "redirect:/company/list";
    }


}
