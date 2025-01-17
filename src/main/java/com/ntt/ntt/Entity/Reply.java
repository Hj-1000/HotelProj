package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="reply")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reply extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer replyId;
    // 답변 내용
    @Column(length = 255, nullable = false)
    private String replyContent;

    @ManyToOne
    @JoinColumn(name = "qnaId")
    private Qna qna;

}
