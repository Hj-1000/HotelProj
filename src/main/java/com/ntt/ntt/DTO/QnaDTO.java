package com.ntt.ntt.DTO;

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
public class QnaDTO {

    private Integer qnaId;

    private String qnaTitle;

    private String qnaContent;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
