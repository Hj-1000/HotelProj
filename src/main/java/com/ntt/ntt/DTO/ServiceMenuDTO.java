package com.ntt.ntt.DTO;

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

    private String serviceMenuName;

    private String serviceMenuInfo;

    private String serviceMenuStatus;

    private Integer serviceMenuPrice;

    private ServiceCate serviceCateId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    @Builder.Default
    private List<ImageDTO> serviceMenuImageDTOList = new ArrayList<>();
}
