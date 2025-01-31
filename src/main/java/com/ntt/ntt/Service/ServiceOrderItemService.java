package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.ServiceOrderItemDTO;
import com.ntt.ntt.Entity.ServiceOrderItem;
import com.ntt.ntt.Repository.ServiceOrderItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ServiceOrderItemService {
    private final ServiceOrderItemRepository serviceOrderItemRepository;
    private final ModelMapper modelMapper;

    // 특정 주문의 아이템 목록 조회
    public List<ServiceOrderItemDTO> getItemsByOrderId(Integer serviceOrderId) {
        List<ServiceOrderItem> serviceOrderItems = serviceOrderItemRepository.findByServiceOrder_ServiceOrderId(serviceOrderId);
        if (serviceOrderItems.isEmpty()) {
            throw new RuntimeException("주문 아이템이 없습니다.");
        }
        return serviceOrderItems.stream()
                .map(serviceOrderItem -> modelMapper.map(serviceOrderItem, ServiceOrderItemDTO.class))
                .collect(Collectors.toList());
    }
}

