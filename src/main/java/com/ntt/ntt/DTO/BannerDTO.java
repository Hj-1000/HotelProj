package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Member;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BannerDTO {

    private Integer bannerId;

    private String bannerTitle;

    private Boolean bannerStatus;

    private Integer bannerRank;

    private Member memberId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
