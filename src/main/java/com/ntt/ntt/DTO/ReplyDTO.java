package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Qna;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ReplyDTO {

    private Integer replyId;

    private String replyContent;

    private Qna qnaId;

    // regDate와 modDate는 엔티티에서 자동으로 관리되므로 제거함
}
