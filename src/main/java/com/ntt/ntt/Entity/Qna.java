package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="qna")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Qna extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer qnaId;
    // Q&A 제목
    @Column(length = 50, nullable = false)
    private String qnaTitle;
    // Q&A 내용
    @Column(length = 255, nullable = false)
    private String qnaContent;

    @ManyToOne
    @JoinColumn(name = "memberId")
    private Member member;
}
