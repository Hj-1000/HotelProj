package com.ntt.ntt.DTO;

import com.ntt.ntt.Constant.ServiceMenuStatus;
import com.ntt.ntt.Entity.ServiceCate;
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
public class ServiceMenuDTO {

    private Integer serviceMenuId;

    private String serviceCateName; //카테고리의 이름으로 불러오기 위함 2025-01-23 추가

    private String serviceMenuName;

    private String serviceMenuInfo;

    private ServiceMenuStatus serviceMenuStatus;

    private Integer serviceMenuPrice;

    private ServiceCate serviceCateId; //카테고리와 연결

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    @Builder.Default
    private List<ImageDTO> serviceMenuImageDTOList = new ArrayList<>();
    // TODO:25-01-24 정상 코드로 복귀 이거 잘 되는거 확인했으면 이 주석은 지울것
}
