package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Qna;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ReplyDTO {

    private Integer replyId;

    private String replyContent;

    private Qna qnaId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;
}
