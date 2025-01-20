package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Member;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
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
    private List<ImageDTO> companyImgDTOList = new ArrayList<>();

}
