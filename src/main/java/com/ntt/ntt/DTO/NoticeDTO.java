package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Room;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDTO {

    private Integer noticeId;

    private String noticeTitle;

    private String noticeContent;

    private String noticeImg;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
