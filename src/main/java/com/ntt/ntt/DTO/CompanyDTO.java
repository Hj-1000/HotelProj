package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Member;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder //2025-01-21 추가
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDTO {

    private Integer companyId;

    private String companyName;

    private String companyManager;

    private Member memberId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    @Builder.Default
    private List<ImageDTO> companyImgDTOList = new ArrayList<>(); //이미지 2025-01-21추가

    private Integer hotelCount; //지사 수 2025-01-22 추가

    public CompanyDTO(Integer companyId, String companyName) {
        this.companyId = companyId;
        this.companyName = companyName;
    }
}
