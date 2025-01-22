package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Qna;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class QnaDTO {

    private Integer qnaId;

    @NotEmpty(message = "제목은 비어 있을 수 없습니다.")
    private String qnaTitle;

    @NotEmpty(message = "내용은 비어 있을 수 없습니다.")
    private String qnaContent;

    private String memberName;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    // QnaDTO에서 Qna로 변환하는 메서드 예시
    public QnaDTO dtoToEntity(Qna qna) {
        QnaDTO qnaDTO = new QnaDTO();
        qnaDTO.setQnaId(qna.getQnaId());
        qnaDTO.setQnaTitle(qna.getQnaTitle());
        qnaDTO.setQnaContent(qna.getQnaContent());
        qnaDTO.setRegDate(qna.getRegDate());
        qnaDTO.setModDate(qna.getModDate());
        qnaDTO.setMemberName(qna.getMember().getMemberName());  // 작성자 이름 설정

        return qnaDTO;
    }
    }

