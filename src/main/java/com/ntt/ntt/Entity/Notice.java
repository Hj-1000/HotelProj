package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="notice")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer noticeId;
    // 공지사항 제목
    @Column(length = 50, nullable = false)
    private String noticeTitle;
    // 공지사항 내용
    @Column(length = 255, nullable = false)
    private String noticeContent;
}
