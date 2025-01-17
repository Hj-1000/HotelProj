package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.ServiceCate;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServiceMenuDTO {

    private Integer serviceMenuId;

    private String serviceMenuName;

    private String serviceMenuInfo;

    private String serviceMenuStatus;

    private Integer serviceMenuPrice;

    private ServiceCate serviceCateId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
