package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Notice;
import com.ntt.ntt.Entity.Room;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class NoticeDTO {

    private Integer noticeId;

    private String noticeTitle;

    private String noticeContent;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    public NoticeDTO(Notice notice) {
        this.noticeId = notice.getNoticeId();
        this.noticeTitle = notice.getNoticeTitle();
        this.noticeContent = notice.getNoticeContent();
        this.regDate = notice.getRegDate();
        this.modDate = notice.getModDate();
    }
    @Builder.Default
    private List<ImageDTO> noticeImageDTOList = new ArrayList<>();



}
