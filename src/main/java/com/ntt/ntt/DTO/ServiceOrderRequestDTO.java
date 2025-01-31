package com.ntt.ntt.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ServiceOrderRequestDTO {
    private Integer memberId;
    private Integer roomId;
    private List<ServiceOrderItemDTO> orderItems;
}
