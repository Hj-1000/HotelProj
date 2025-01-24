package com.ntt.ntt.Service;

import com.ntt.ntt.Constant.ServiceOrderStatus;
import com.ntt.ntt.DTO.ServiceOrderDTO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ServiceOrderService {
    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderItemRepository serviceOrderItemRepository;
    private final ModelMapper modelMapper;

    // 특정 회원의 주문 목록 조회
    public List<ServiceOrderDTO> getOrdersByMemberId(Integer memberId) {
        List<ServiceOrder> serviceOrders = serviceOrderRepository.findByMember_MemberId(memberId);
        if (serviceOrders.isEmpty()) {
            throw new RuntimeException("주문 목록이 비어 있습니다.");
        }
        return serviceOrders.stream()
                .map(serviceOrder -> modelMapper.map(serviceOrder, ServiceOrderDTO.class))
                .collect(Collectors.toList());
    }

    // 주문 생성
    public ServiceOrderDTO createOrder(Integer memberId, Integer roomId, List<ServiceOrderItemDTO> orderItems) {
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setMember(serviceOrder.getMember());
        serviceOrder.setRoom(serviceOrder.getRoom());
        serviceOrder.setServiceOrderStatus(ServiceOrderStatus.PENDING); // 기본적으로 보류 상태로 설정
        serviceOrder.setServiceOrderItemList(new ArrayList<>()); // 주문 아이템 리스트 초기화

        for (ServiceOrderItemDTO itemDTO : orderItems) {
            ServiceMenu serviceMenu = new ServiceMenu();
            serviceMenu.setServiceMenuId(itemDTO.getServiceMenuId());
            ServiceOrderItem serviceOrderItem = modelMapper.map(itemDTO, ServiceOrderItem.class);
            serviceOrderItem.setServiceMenu(serviceMenu);
            serviceOrderItem.setOrderPrice(itemDTO.getOrderCount() * serviceMenu.getServiceMenuPrice()); // 가격 계산
            serviceOrder.addServiceOrderItem(serviceOrderItem); // 주문에 아이템 추가
        }

        serviceOrderRepository.save(serviceOrder);
        return modelMapper.map(serviceOrder, ServiceOrderDTO.class);
    }

    // 주문 상태 변경
    public ServiceOrderDTO updateOrderStatus(Integer serviceOrderId, ServiceOrderStatus status) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        serviceOrder.setServiceOrderStatus(status); // 상태 변경
        serviceOrderRepository.save(serviceOrder);
        return modelMapper.map(serviceOrder, ServiceOrderDTO.class);
    }
}

