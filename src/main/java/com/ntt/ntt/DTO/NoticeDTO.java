package com.ntt.ntt.DTO;

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

    @Builder.Default
    private List<ImageDTO> noticeImageDTOList = new ArrayList<>();



}
