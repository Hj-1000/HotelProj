package com.ntt.ntt.Controller;

import com.ntt.ntt.Util.PaginationUtil;
import com.ntt.ntt.DTO.NoticeDTO;
import com.ntt.ntt.Service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Log
public class NoticeController {
    private final NoticeService noticeService;


    @Autowired
    private PaginationUtil paginationUtil;

    @GetMapping("/notice")
    public String notice(){
        return "redirect:/notice/list";
    }

    @GetMapping("/notice/list")
    public String listNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // 페이징된 공지사항 데이터 조회
        Page<NoticeDTO> noticePage = noticeService.getNotices(page, size);

        // PaginationUtil을 사용하여 페이징 정보 가져오기
        Map<String, Integer> pagination = paginationUtil.pagination(noticePage);

        // 모델에 데이터를 추가
        model.addAttribute("noticeDTOList", noticePage.getContent());
        model.addAttribute("pagination", pagination);

        return "notice/list";
    }

//    @GetMapping("/notice/list")
//    public String getAllnotice2(
//            @RequestParam(required = false, defaultValue = "") String noticeTitle,
//            @RequestParam(required = false, defaultValue = "") String noticeContent,
//            @RequestParam(required = false, defaultValue = "") String regDate,
//            @RequestParam(required = false, defaultValue = "") String modDate,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            Model model) {
//        try {
//            // 필터링된 리스트 가져오기
//            List<NoticeDTO> filteredNotice = noticeService.getFilteredNotice(noticeTitle, noticeContent, regDate, modDate);
//
//            // 페이징 처리
//            int startIdx = page * size;
//            int endIdx = Math.min(startIdx + size, filteredNotice.size());
//            List<NoticeDTO> pagedNotice = filteredNotice.subList(startIdx, endIdx);
//
//            model.addAttribute("noticeDTOList", pagedNotice);
//            model.addAttribute("pageNumber", page);
//            model.addAttribute("totalPages", (int) Math.ceil((double) filteredNotice.size() / size));
//            model.addAttribute("size", size);
//        } catch (Exception e) {
//            model.addAttribute("error", "목록을 가져오는 중 오류가 발생했습니다.");
//            e.printStackTrace();
//        }
//        model.addAttribute("noticeTitle", noticeTitle);
//        return "notice/list";
//    }
//    @GetMapping("/notice/list")
//    public String getAllnotice(@RequestParam(required = false) String noticeTitle,
//                               @RequestParam(required = false) String noticeContent,
//                               @RequestParam(required = false) String regDate,
//                               @RequestParam(required = false) String modDate,
//                               @RequestParam(defaultValue = "0") int page,  // 추가됨
//                               @RequestParam(defaultValue = "10") int size, // 추가됨
//                                Model model) {
//        try {
//            // 필터링된 리스트 가져오기
//            List<NoticeDTO> filteredNotice = noticeService.getFilteredNotice(noticeTitle,noticeContent,regDate, modDate);
//
//            // 페이징 처리
//            int startIdx = page * size;
//            int endIdx = Math.min(startIdx + size, filteredNotice.size());
//            List<NoticeDTO> pagedNotice = filteredNotice.subList(startIdx, endIdx);
//
//            model.addAttribute("noticeDTOList", pagedNotice);
//            model.addAttribute("pageNumber", page);
//            model.addAttribute("totalPages", (int) Math.ceil((double) filteredNotice.size() / size));
//            model.addAttribute("size", size);
//
//        } catch (Exception e) {
//            model.addAttribute("error", "목록을 가져오는 중 오류가 발생했습니다.");
//            e.printStackTrace();
//        }
//        return "notice/list";
//    }
//    @GetMapping("/notice/list")
//    public String listForm(Model model){
//        List<NoticeDTO> noticeDTOList = noticeService.list();
//
//        model.addAttribute("noticeDTOList", noticeDTOList);
//        return "notice/list";
//    }

    @GetMapping("/notice/register")
    public String registerForm(Model model){
        model.addAttribute("noticeDTO", new NoticeDTO());
        return "notice/register";
    }

    @PostMapping("/notice/register")
    public String registerProc(NoticeDTO noticeDTO, @RequestParam("multipartFile") List<MultipartFile> multipartFile){

        noticeService.register(noticeDTO, multipartFile);
        return "redirect:/notice/list";
    }

    @GetMapping("/notice/read")
    public String readForm(@RequestParam Integer noticeId, Model model){
        NoticeDTO noticeDTO = noticeService.read(noticeId);

        model.addAttribute("noticeDTO", noticeDTO);
        return "notice/read";
    }
    @GetMapping("/notice/update")
    public String updateForm(@RequestParam Integer noticeId, Model model){
        NoticeDTO noticeDTO = noticeService.read(noticeId);
        model.addAttribute("noticeDTO", noticeDTO);
        return "notice/update";
    }
    @PostMapping("/notice/update")
    public String updateProc(@ModelAttribute NoticeDTO noticeDTO,@RequestParam("multipartFile") List<MultipartFile> multipartFile){
        noticeService.update(noticeDTO, multipartFile);
        return "redirect:/notice/list";
    }
    @GetMapping("/notice/delete")
    public String deleteForm(@RequestParam Integer noticeId, List<MultipartFile> multipartFile){
        noticeService.delete(noticeId);
        return "redirect:/notice/list";
    }
    @GetMapping("/notice/userlist")
    public String userlistForm(Model model){
        List<NoticeDTO> noticeDTOList = noticeService.list();
        model.addAttribute("noticeDTOList", noticeDTOList);
        return "notice/userlist";
    }
    @GetMapping("/notice/userread")
    public String userreadForm(@RequestParam Integer noticeId, Model model) {
        NoticeDTO noticeDTO = noticeService.read(noticeId);
        model.addAttribute("noticeDTO", noticeDTO);
        return "notice/userread";
    }
}




