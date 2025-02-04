package com.ntt.ntt.Service;

import com.ntt.ntt.Constant.ServiceOrderStatus;
import com.ntt.ntt.DTO.ServiceOrderDTO;
import com.ntt.ntt.DTO.ServiceOrderItemDTO;
import com.ntt.ntt.Entity.*;
import com.ntt.ntt.Repository.*;
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
    private final ServiceMenuRepository serviceMenuRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
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
        //  회원 및 방 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다: " + memberId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("방 정보를 찾을 수 없습니다: " + roomId));

        //  주문 생성
        ServiceOrder serviceOrder = ServiceOrder.builder()
                .member(member)
                .room(room)
                .serviceOrderStatus(ServiceOrderStatus.PENDING) // 기본 주문 상태
                .build();

        List<ServiceOrderItem> orderItemList = new ArrayList<>();
        int totalPrice = 0;

        for (ServiceOrderItemDTO itemDTO : orderItems) {
            ServiceMenu menu = serviceMenuRepository.findById(itemDTO.getServiceMenuId())
                    .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다: " + itemDTO.getServiceMenuId()));

            if (menu.getServiceMenuPrice() == null) {
                throw new RuntimeException("메뉴 가격이 설정되지 않았습니다: " + menu.getServiceMenuName());
            }

            //  주문 아이템 생성
            ServiceOrderItem orderItem = ServiceOrderItem.builder()
                    .serviceOrder(serviceOrder)
                    .serviceMenu(menu)
                    .orderCount(itemDTO.getOrderCount())
                    .orderPrice(menu.getServiceMenuPrice() * itemDTO.getOrderCount())
                    .build();

            totalPrice += orderItem.getOrderPrice();
            orderItemList.add(orderItem);
        }

        // 주문 정보 저장
        serviceOrder.setServiceOrderItemList(orderItemList);
        serviceOrder.calculateTotalPrice(); // 총 금액 계산
        serviceOrderRepository.save(serviceOrder);

        //  DTO 변환 후 반환
        return ServiceOrderDTO.fromEntity(serviceOrder);
    }

    // 주문 상태 변경
    public ServiceOrderDTO updateOrderStatus(Integer serviceOrderId, ServiceOrderStatus status) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        serviceOrder.setServiceOrderStatus(status); // 상태 변경
        serviceOrderRepository.save(serviceOrder);
        return modelMapper.map(serviceOrder, ServiceOrderDTO.class);
    }

    //2025.02.03 추가한 기능들
    // 주문 취소 기능
    public void cancelOrder(Integer serviceOrderId) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        serviceOrder.setServiceOrderStatus(ServiceOrderStatus.CANCELED);
        serviceOrderRepository.save(serviceOrder);
    }

    // 특정 주문 상세 조회 (사용자 & 관리자용 공통)
    public ServiceOrderDTO getOrderDetails(Integer serviceOrderId) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        return modelMapper.map(serviceOrder, ServiceOrderDTO.class);
    }

    // 모든 주문 목록 조회 (관리자용)
    public List<ServiceOrderDTO> getAllOrders() {
        List<ServiceOrder> serviceOrders = serviceOrderRepository.findAll();
        return serviceOrders.stream()
                .map(order -> modelMapper.map(order, ServiceOrderDTO.class))
                .collect(Collectors.toList());
    }

    // 주문 처리 기능 (예: 조리 중, 배달 중 등 상태 변경)
    public ServiceOrderDTO processOrder(Integer serviceOrderId, ServiceOrderStatus status) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        serviceOrder.setServiceOrderStatus(status);
        serviceOrderRepository.save(serviceOrder);
        return modelMapper.map(serviceOrder, ServiceOrderDTO.class);
    }

}

