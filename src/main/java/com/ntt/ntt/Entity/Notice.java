package com.ntt.ntt.Entity;

import com.ntt.ntt.DTO.NoticeDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="notice")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SequenceGenerator(
        name = "notice_seq",
        sequenceName = "notice_seq",
        initialValue = 1,
        allocationSize = 1
)
public class Notice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notice_seq")
//            IDENTITY)
    private Integer noticeId;
    // 공지사항 제목
    @Column(length = 50, nullable = false)
    private String noticeTitle;
    // 공지사항 내용
    @Column(length = 255, nullable = false)
    private String noticeContent;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Image> noticeImageList;

    public Notice(Integer noticeId) {
        this.noticeId = noticeId;

    }

    public NoticeDTO toDTO() {
        return NoticeDTO.builder()
                .noticeId(this.noticeId)
                .noticeTitle(this.noticeTitle)
                .noticeContent(this.noticeContent)
                .regDate(this.getRegDate())
                .modDate(this.getModDate())
                .build();
    }
}