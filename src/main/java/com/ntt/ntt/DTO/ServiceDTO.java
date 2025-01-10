package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDTO {

    private Integer roomId;

    private String serviceName;

    private String serviceInfo;

    private Integer servicePrice;

    private String serviceImg;

    private Hotel hotelId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
