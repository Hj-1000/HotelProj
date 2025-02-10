package com.ntt.ntt.Controller;

import com.ntt.ntt.Util.PaginationUtil;
import com.ntt.ntt.DTO.NoticeDTO;
import com.ntt.ntt.Service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Log
@Tag(name = "NoticeController", description = "notice페이지")
public class NoticeController {
    private final NoticeService noticeService;


    @Autowired
    private PaginationUtil paginationUtil;
    @Operation(summary = "이동폼", description = "리스트 페이지로 이동한다.")
    @GetMapping("/notice")
    public String notice(){
        return "redirect:/notice/list";
    }
    @Operation(summary = "이동폼", description = "리스트 페이지로 이동한다.")
    @GetMapping("/notice/list")
    public String listNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // 페이징된 공지사항 데이터 조회
        Page<NoticeDTO> noticePage = noticeService.getNotices(page,size);

        // PaginationUtil을 사용하여 페이징 정보 가져오기
        Map<String, Integer> pagination = paginationUtil.pagination(noticePage);

        // 전체 페이지 수
        int totalPages = noticePage.getTotalPages();

        // 현재 페이지 번호
        int currentPage = pagination.get("currentPage");

        // 시작 페이지와 끝 페이지 계산 (현재 페이지를 기준으로 최대 10페이지까지)
        int startPage = Math.max(1, currentPage - 4); // 10개씩 끊어서 시작 페이지 계산
        int endPage = Math.min(startPage + 9, totalPages); // 최대 10페이지까지, 전체 페이지 수를 넘지 않도록

        // 페이지 정보 업데이트
        pagination.put("startPage", startPage);
        pagination.put("endPage", endPage);

        // 모델에 데이터를 추가
        model.addAttribute("noticeDTOList", noticePage.getContent());
        model.addAttribute("pagination", pagination);

        return "notice/list";
    }



    @Operation(summary = "등록폼", description = "공지사항 등록폼 페이지로 이동한다.")
    @GetMapping("/notice/register")

    public String registerForm(@AuthenticationPrincipal UserDetails userDetails, Model model){
        if (userDetails == null) {
            model.addAttribute("errorMessage","로그인 후 글을 남길수 있습니다");
            return "redirect:/login";
        }
        model.addAttribute("noticeDTO", new NoticeDTO());
        return "notice/register";


    }
    @Operation(summary = "등록창", description = "공지사항 등록 후 리스트 페이지로 이동한다.")
    @PostMapping("/notice/register")
    public String registerProc(NoticeDTO noticeDTO, @RequestParam("multipartFile") List<MultipartFile> multipartFile){

        noticeService.register(noticeDTO, multipartFile);
        return "redirect:/notice/list";
    }
    @Operation(summary = "이동폼", description = "공지사항 상세보기 페이지로 이동한다.")
    @GetMapping("/notice/read")
    public String readForm(@RequestParam Integer noticeId, Model model){
        NoticeDTO noticeDTO = noticeService.read(noticeId);

        model.addAttribute("noticeDTO", noticeDTO);
        return "notice/read";
    }
    @Operation(summary = "수정폼", description = "공지사항 수정 페이지로 이동한다.")
    @GetMapping("/notice/update")
    public String updateForm(@RequestParam Integer noticeId, Model model){
        NoticeDTO noticeDTO = noticeService.read(noticeId);
        model.addAttribute("noticeDTO", noticeDTO);
        return "notice/update";
    }
    @Operation(summary = "수정창", description = "공지사항 수정후 리스트 페이지로 이동한다.")
    @PostMapping("/notice/update")
    public String updateProc(@ModelAttribute NoticeDTO noticeDTO,@RequestParam("multipartFile") List<MultipartFile> multipartFile){
        noticeService.update(noticeDTO, multipartFile);
        return "redirect:/notice/list";
    }
    @Operation(summary = "삭제폼", description = "공지사항 삭제 후 리스트 페이지로 이동한다.")
    @GetMapping("/notice/delete")
    public String deleteForm(@RequestParam Integer noticeId, List<MultipartFile> multipartFile){
        noticeService.delete(noticeId);
        return "redirect:/notice/list";
    }
    @Operation(summary = "유저 공지사항 이동폼", description = "유저 공지사항 리스트 페이지로 이동한다.")
//    @GetMapping("/notice/userlist")
//    public String userlistForm(Model model){
//        List<NoticeDTO> noticeDTOList = noticeService.list();
//        model.addAttribute("noticeDTOList", noticeDTOList);
//        return "notice/userlist";
//    }
    @GetMapping("/notice/userlist")
    public String listNoticess(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // 페이징된 공지사항 데이터 조회
        Page<NoticeDTO> noticePage = noticeService.getNotices(page,size);

        // PaginationUtil을 사용하여 페이징 정보 가져오기
        Map<String, Integer> pagination = paginationUtil.pagination(noticePage);

        // 전체 페이지 수
        int totalPages = noticePage.getTotalPages();

        // 현재 페이지 번호
        int currentPage = pagination.get("currentPage");

        // 시작 페이지와 끝 페이지 계산 (현재 페이지를 기준으로 최대 10페이지까지)
        int startPage = Math.max(1, currentPage - 4); // 10개씩 끊어서 시작 페이지 계산
        int endPage = Math.min(startPage + 9, totalPages); // 최대 10페이지까지, 전체 페이지 수를 넘지 않도록

        // 페이지 정보 업데이트
        pagination.put("startPage", startPage);
        pagination.put("endPage", endPage);

        // 모델에 데이터를 추가
        model.addAttribute("noticeDTOList", noticePage.getContent());
        model.addAttribute("pagination", pagination);

        return "notice/userlist";
    }
    @Operation(summary = "유저 상세보기 이동폼", description = "유저 공지사항 상세보기 페이지로 이동한다.")
    @GetMapping("/notice/userread")
    public String userreadForm(@RequestParam Integer noticeId, Model model) {
        NoticeDTO noticeDTO = noticeService.read(noticeId);
        model.addAttribute("noticeDTO", noticeDTO);
        return "notice/userread";
    }
    @GetMapping("/sample/hotellist")
    public String hotellistForm() {
        return "sample/hotellist";
    }
    @GetMapping("/sample/hotelread")
    public String hotelreadForm() {
        return "sample/hotelread";
    }
}




