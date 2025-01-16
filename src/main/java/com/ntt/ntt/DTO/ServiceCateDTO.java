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
public class ServiceCateDTO {

    private Integer serviceCateId;

    private String serviceCateName;

    private String serviceCateImg;

    private Hotel hotelId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
