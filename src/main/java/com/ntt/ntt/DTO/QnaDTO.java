package com.ntt.ntt.DTO;

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

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
