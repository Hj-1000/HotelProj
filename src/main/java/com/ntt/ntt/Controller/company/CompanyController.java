package com.ntt.ntt.Controller.company;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Service.ImageService;
import com.ntt.ntt.Service.company.CompanyService;
import com.ntt.ntt.Util.PaginationUtil;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String read(Integer companyId, Model model) {
        CompanyDTO companyDTO = companyService.read(companyId);
        model.addAttribute("companyDTO", companyDTO);
        return "/company/read";
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
    public String modifyService(CompanyDTO companyDTO) {
        companyService.update(companyDTO);
        return "redirect:/company/list";
    }

    //삭제
    @GetMapping("/delete")
    public String delete(Integer companyId) {
        companyService.delete(companyId);
        return "redirect:/company/list";
    }


}
