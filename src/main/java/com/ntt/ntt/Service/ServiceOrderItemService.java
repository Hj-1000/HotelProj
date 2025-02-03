package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.ServiceOrderItemDTO;
import com.ntt.ntt.Entity.ServiceMenu;
import com.ntt.ntt.Entity.ServiceOrder;
import com.ntt.ntt.Entity.ServiceOrderItem;
import com.ntt.ntt.Repository.ServiceOrderItemRepository;
import com.ntt.ntt.Repository.ServiceOrderRepository;
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
    private final ServiceOrderRepository serviceOrderRepository;
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

    //2025.02.03작성
    //서비스 주문 추가기능
    public ServiceOrderItemDTO addOrderItem(Integer serviceOrderId, ServiceOrderItemDTO itemDTO) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        ServiceMenu serviceMenu = new ServiceMenu();
        serviceMenu.setServiceMenuId(itemDTO.getServiceMenuId());

        ServiceOrderItem serviceOrderItem = modelMapper.map(itemDTO, ServiceOrderItem.class);
        serviceOrderItem.setServiceOrder(serviceOrder);
        serviceOrderItem.setServiceMenu(serviceMenu);
        serviceOrderItem.setOrderPrice(itemDTO.getOrderCount() * serviceMenu.getServiceMenuPrice());

        serviceOrderItemRepository.save(serviceOrderItem);
        return modelMapper.map(serviceOrderItem, ServiceOrderItemDTO.class);
    }

    //서비스 주문 수정기능
    public ServiceOrderItemDTO updateOrderItemCount(Integer serviceOrderItemId, Integer newCount) {
        ServiceOrderItem serviceOrderItem = serviceOrderItemRepository.findById(serviceOrderItemId)
                .orElseThrow(() -> new RuntimeException("주문 아이템을 찾을 수 없습니다."));

        serviceOrderItem.setOrderCount(newCount);
        serviceOrderItem.setOrderPrice(serviceOrderItem.getServiceMenu().getServiceMenuPrice() * newCount);

        serviceOrderItemRepository.save(serviceOrderItem);
        return modelMapper.map(serviceOrderItem, ServiceOrderItemDTO.class);
    }
    //서비스 주문 삭제 기능
    public void deleteOrderItem(Integer serviceOrderItemId) {
        if (!serviceOrderItemRepository.existsById(serviceOrderItemId)) {
            throw new RuntimeException("해당 주문 아이템이 존재하지 않습니다.");
        }
        serviceOrderItemRepository.deleteById(serviceOrderItemId);
    }
}

