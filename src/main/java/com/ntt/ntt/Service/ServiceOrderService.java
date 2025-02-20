package com.ntt.ntt.Service;

import com.ntt.ntt.Constant.ServiceOrderStatus;
import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.DTO.ServiceOrderDTO;
import com.ntt.ntt.DTO.ServiceOrderHistoryDTO;
import com.ntt.ntt.DTO.ServiceOrderItemDTO;
import com.ntt.ntt.Entity.*;
import com.ntt.ntt.Repository.*;
import groovy.util.logging.Log4j2;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceMenuRepository serviceMenuRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository; //2025-02-18 room과 결합
    private final ReservationRepository reservationRepository;


    //주문 ServiceOrder, ServiceOrderItem
    //위의 내용이 있는 주문 리스트 필요
    //단 주문 목록이 들어있는 경우 누구의 주문인지 알기 위해 email을 받는다.

    //내 주문이 맞는지 체크부터 함
    public boolean validateOrder(Integer serviceOrderId, String memberEmail) {

        Member member =
                memberRepository.findByEmail(memberEmail);

        ServiceOrder serviceOrder =
                serviceOrderRepository.findById(serviceOrderId)
                        .orElseThrow(EntityNotFoundException::new);

        // 주문 id로 찾은 주문테이블의 회원 참조 memberId와 현재 로그인 한 사람을 비교
        if (!StringUtils.equals(member.getMemberEmail(), serviceOrder.getMember().getMemberEmail())) {
            return false;
        }
        return true;
    }

    //주문을 취소
    public void cancelOrder(Integer serviceOrderId) {
        // 삭제할 번호를 받아서 삭제
        ServiceOrder serviceOrder =
                serviceOrderRepository.findById(serviceOrderId)
                        .orElseThrow(EntityNotFoundException::new);
        //주문상태인 orderStatus를 주문 취소 상태로 변경
        serviceOrder.setServiceOrderStatus(ServiceOrderStatus.CANCELED);
        //주문의 자식인 주문아이템들의 주문 수량을 가지고 serviceMenu의 주문수량에 더한다.

        for (ServiceOrderItem serviceOrderItem : serviceOrder.getServiceOrderItemList()) {
            serviceOrderItem.getServiceMenu().setServiceMenuQuantity(
                    serviceOrderItem.getServiceMenu().getServiceMenuQuantity() + serviceOrderItem.getCount()
            );
        }
        if (serviceOrder != null) {
            serviceOrderRepository.deleteById(serviceOrderId);
        }
    }
    //todo: 이제 멤버뿐만아니라 roomId도 받아서 셋해야함
    public Integer createOrder(ServiceOrderDTO serviceOrderDTO, String memberEmail , Integer roomId) {
        //현재 선택한 serviceMenuId 는 serviceOrderDTO로 들어온다. 이 값으로 판매중인 serviceMenu Entity를 가져온다.
        ServiceMenu serviceMenu =
                serviceMenuRepository.findById(serviceOrderDTO.getServiceMenuId()).orElseThrow(EntityNotFoundException::new);
        //사려는 메뉴를 찾지 못했다면 예외처리

        //email을 통해서 현재 로그인한 사용자를 가져옴
        Member member =
                memberRepository.findByEmail(memberEmail);

        Room room = roomRepository.findById(roomId).orElseThrow(EntityNotFoundException::new);

        //조건 : 현재 판매중인 item의 수량이 구매하려는 수량보다 크거나 같아야 함
        if (serviceMenu.getServiceMenuQuantity() >= serviceOrderDTO.getCount()) {
            //serviceOrderItem은 구매하려는 아이템들이고 구매하이템을은 구매목록을 참조
            //serviceOrderItem을 생성
            ServiceOrderItem serviceOrderItem = new ServiceOrderItem();
            serviceOrderItem.setServiceMenu(serviceMenu); //구매한 메뉴
            serviceOrderItem.setCount(serviceOrderDTO.getCount()); //수량
            serviceOrderItem.setOrderPrice(serviceMenu.getServiceMenuPrice()); //구매한 메뉴의 금액

            //판매하는 아이템의 수량은 구매수량을 뺀 수량으로 변경해야함
            serviceMenu.setServiceMenuQuantity(serviceMenu.getServiceMenuQuantity() - serviceOrderDTO.getCount());

            //주문 아이템들이 들어갈 주문 테이블을 만든다. 주문 아이템들이 참조하는 주문
            ServiceOrder serviceOrder = new ServiceOrder();
            serviceOrder.setMember(member); //구매한 사람의 id로 찾아온 entity객체
            serviceOrder.setRoom(room); //예약을 통해 방의 정보를 가져옴
            serviceOrder.setServiceOrderItemList(serviceOrderItem); //주문목록 이건 새로 만든 setOrderItemList이다.

            serviceOrder.setServiceOrderStatus(ServiceOrderStatus.COMPLETED); //주문상태
            serviceOrder.setRegDate(LocalDateTime.now()); //주문시간
            //이렇게 만들어진 order 주문 객체를 저장하기전에
            //serviceOrderItem에서 private ServiceOrder serviceOrder; 를 set 해줌으로써
            //양방향이기에 같이 등록되며 같이 등록될 때 pk값도 같이 참조
            serviceOrderItem.setServiceOrder(serviceOrder);

            //실제 저장은 serviceOrder만 하지만 serviceOrder에
            //    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL,
            //            orphanRemoval = true, fetch = FetchType.LAZY)
            //    private List<ServiceOrderItem> serviceOrderItemList = new ArrayList<>();
            // 이렇게 List를 만들어주었고 set해줬기 때문에 둘다 저장이 된다.
            serviceOrder =
                    serviceOrderRepository.save(serviceOrder);

            return serviceOrder.getServiceOrderId();
        } else {
            return null;
        }

    }

    public Integer orders(List<ServiceOrderDTO> serviceOrderDTOList, String memberEmail, Integer roomId) {
        //주문을 했다면 판매하고 있는 상품의 수량을 변경

        Member member = memberRepository.findByEmail(memberEmail);
        Room room = roomRepository.findById(roomId).orElseThrow(EntityNotFoundException::new);
        List<ServiceOrderItem> serviceOrderItemList = new ArrayList<>();
        ServiceOrder serviceOrder = new ServiceOrder();

        for (ServiceOrderDTO serviceOrderDTO : serviceOrderDTOList) {
            ServiceMenu serviceMenu =
                    serviceMenuRepository.findById(serviceOrderDTO.getServiceMenuId())
                            .orElseThrow(EntityNotFoundException::new);

            ServiceOrderItem serviceOrderItem = new ServiceOrderItem();
            serviceOrderItem.setServiceMenu(serviceMenu);
            serviceOrderItem.setCount(serviceOrderDTO.getCount());
            serviceOrderItem.setOrderPrice(serviceMenu.getServiceMenuPrice());
            serviceOrderItem.setServiceOrder(serviceOrder);

            serviceMenu.setServiceMenuQuantity(serviceMenu.getServiceMenuQuantity() - serviceOrderDTO.getCount());

            serviceOrderItemList.add(serviceOrderItem);
        }
        serviceOrder.setMember(member);
        serviceOrder.setRoom(room);
        serviceOrder.setServiceOrderStatus(ServiceOrderStatus.COMPLETED);
        serviceOrder.setRegDate(LocalDateTime.now());
        serviceOrder.setServiceOrderItemList(serviceOrderItemList);

        serviceOrderRepository.save(serviceOrder);

        return serviceOrder.getServiceOrderId();
    }

    // 구매이력 불러오기
    public Page<ServiceOrderHistoryDTO> getOrderList(String memberEmail, Pageable pageable) {
        //repository에서 필요한 memberEmail

        //구매목록
        List<ServiceOrder> serviceOrderList = serviceOrderRepository.findServiceOrders(memberEmail, pageable);

        //페이징처리를 위한 총 구매목록의 수
        Integer totalCount = serviceOrderRepository.totalCount(memberEmail);

        //구매목록의 구매아이템들을 만들어주기 위한 List
        List<ServiceOrderHistoryDTO> serviceOrderHistoryDTOList = new ArrayList<>();

        //EntityToDTO // 주문, 주문아이템들, 주문아이템들의 이미지
        for (ServiceOrder serviceOrder : serviceOrderList) {
            ServiceOrderHistoryDTO serviceOrderHistoryDTO = new ServiceOrderHistoryDTO();

            serviceOrderHistoryDTO.setServiceOrderId(serviceOrder.getServiceOrderId());
            serviceOrderHistoryDTO.setRegDate(serviceOrder.getRegDate());
            serviceOrderHistoryDTO.setServiceOrderStatus(serviceOrder.getServiceOrderStatus());

            List<ServiceOrderItem> serviceOrderItemList = serviceOrder.getServiceOrderItemList();

            for (ServiceOrderItem serviceOrderItem : serviceOrderItemList) {

                ServiceOrderItemDTO serviceOrderItemDTO = new ServiceOrderItemDTO();
                serviceOrderItemDTO.setServiceOrderItemId(serviceOrder.getServiceOrderId());
                serviceOrderItemDTO.setServiceMenuName(serviceOrderItem.getServiceMenu().getServiceMenuName());
                serviceOrderItemDTO.setOrderPrice(serviceOrderItem.getOrderPrice());
                serviceOrderItemDTO.setCount(serviceOrderItem.getCount());

                // 아이템 주문아이템들중 1개
                List<Image> serviceMenuImageList = serviceOrderItem.getServiceMenu().getServiceMenuImageList();
                //메뉴에 달려있는 이미지
                for (Image image : serviceMenuImageList) {
                    serviceOrderItemDTO.setImagePath(image.getImagePath());
                }
                serviceOrderHistoryDTO.addServiceOrderItemDTO(serviceOrderItemDTO);
            }
            serviceOrderHistoryDTOList.add(serviceOrderHistoryDTO);
        }
        return new PageImpl<ServiceOrderHistoryDTO>(serviceOrderHistoryDTOList, pageable, totalCount);
    }
}
