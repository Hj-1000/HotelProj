package com.ntt.ntt.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ntt.ntt.Entity.Notice;
import com.ntt.ntt.DTO.NoticeDTO;
import com.ntt.ntt.Repository.NoticeRepository;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
@Service
@Transactional
@RequiredArgsConstructor

public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final ModelMapper modelMapper;

    public void register(NoticeDTO noticeDTO) {
        Notice notice = modelMapper.map(noticeDTO, Notice.class);
        noticeRepository.save(notice);
    }

    public void update(NoticeDTO noticeDTO) {

        Optional<Notice> notice = noticeRepository.findById(noticeDTO.getNoticeId());
        if (notice.isPresent()) {
            Notice notice1 = modelMapper.map(noticeDTO, Notice.class);
            noticeRepository.save(notice1);
        }

    }
    public void delete(Integer noticeId) {
        noticeRepository.deleteById(noticeId);
    }
    public List<NoticeDTO> list() {
        List<Notice> noticeList = noticeRepository.findAll();
        List<NoticeDTO> noticeDTOList = Arrays.asList(modelMapper.map(noticeList, NoticeDTO[].class));
        return noticeDTOList;
    }
    public NoticeDTO read(Integer noticeId) {
        Optional<Notice> notice = noticeRepository.findById(noticeId);
        NoticeDTO noticeDTO = modelMapper.map(notice, NoticeDTO.class);
        return noticeDTO;
    }
}
