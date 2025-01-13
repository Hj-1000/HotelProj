package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Hotel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServiceMenuDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serviceMenuId;

    private String serviceMenuName;

    private String serviceMenuInfo;

    private Integer serviceMenuStatus;

    private Integer serviceMenuPrice;

    private String serviceImg;

}
