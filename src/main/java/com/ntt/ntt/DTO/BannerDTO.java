package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Company;
import com.ntt.ntt.Entity.Member;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BannerDTO {

    private Integer bannerId;

    private String bannerTitle;

    private Boolean bannerStatus;

    private Integer bannerRank;

    private Company companyId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    @Builder.Default
    private List<ImageDTO> bannerImageDTOList = new ArrayList<>();

}
