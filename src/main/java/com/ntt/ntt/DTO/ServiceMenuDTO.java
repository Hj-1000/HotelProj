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

    //2025.02.04추가
    private String imageName;
    private String imagePath;

    @Builder.Default
    private List<ImageDTO> serviceMenuImageDTOList = new ArrayList<>();

}
