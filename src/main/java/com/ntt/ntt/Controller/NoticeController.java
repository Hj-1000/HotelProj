package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.NoticeDTO;
import com.ntt.ntt.Entity.Notice;
import com.ntt.ntt.Service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Log

public class NoticeController {
    private final NoticeService noticeService;

    @GetMapping("/notice")
    public String notice(){
        return "redirect:/notice/list";
    }

    @GetMapping("/notice/list")
    public String listForm(Model model){
        List<NoticeDTO> noticeDTOList = noticeService.list();

        model.addAttribute("noticeDTOList", noticeDTOList);
        return "notice/list";
    }

    @GetMapping("/notice/register")
    public String registerForm(Model model){
        return "notice/register";
    }

    @PostMapping("/notice/register")
    public String registerProc(@ModelAttribute NoticeDTO noticeDTO){

        noticeService.register(noticeDTO);
        return "redirect:/notice/list";
    }

    @GetMapping("/notice/read")
    public String readForm(@RequestParam Integer noticeId, Model model){
        NoticeDTO noticeDTO = noticeService.read(noticeId);
        model.addAttribute("notice", noticeDTO);
        return "notice/read";
    }
    @GetMapping("/notice/update")
    public String updateForm(@RequestParam Integer noticeId, Model model){
        NoticeDTO noticeDTO = noticeService.read(noticeId);
        model.addAttribute("notice", noticeDTO);
        return "notice/update";
    }
    @PostMapping("/notice/update")
    public String updateProc(@ModelAttribute NoticeDTO noticeDTO){
        noticeService.update(noticeDTO);
        return "redirect:/notice/list";
    }
    @GetMapping("/notice/delete")
    public String deleteForm(@RequestParam Integer noticeId){
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
        model.addAttribute("notice", noticeDTO);
        return "notice/userread";
    }
}




