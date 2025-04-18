package com.ntt.ntt.Service;

import com.ntt.ntt.Constant.ServiceOrderStatus;
import com.ntt.ntt.DTO.*;
import com.ntt.ntt.Entity.*;
import com.ntt.ntt.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderItemRepository serviceOrderItemRepository;
    private final ServiceMenuRepository serviceMenuRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;


    //-----------------------------------------------유저용------------------------------
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
            serviceOrderItem.setOrderPrice(0);
        }
//        if (serviceOrder != null) {
//            serviceOrderRepository.deleteById(serviceOrderId);
//        }
    }

    public Integer createOrder(ServiceOrderDTO serviceOrderDTO, String memberEmail, Integer reservationId) {
        //현재 선택한 serviceMenuId 는 serviceOrderDTO로 들어온다. 이 값으로 판매중인 serviceMenu Entity를 가져온다.
        ServiceMenu serviceMenu =
                serviceMenuRepository.findById(serviceOrderDTO.getServiceMenuId()).orElseThrow(EntityNotFoundException::new);
        //사려는 메뉴를 찾지 못했다면 예외처리

        //email을 통해서 현재 로그인한 사용자를 가져옴
        Member member =
                memberRepository.findByEmail(memberEmail);

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(EntityNotFoundException::new);

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
            serviceOrder.setReservation(reservation); //예약을 통해 방의 정보를 가져옴
            serviceOrder.setServiceOrderItemList(serviceOrderItem); //주문목록 이건 새로 만든 setOrderItemList이다.
            serviceOrder.setServiceOrderStatus(ServiceOrderStatus.PENDING); //주문상태는 처음 주문이 들어갔을 때는 아직 결제 전 이므로 주문대기로 생성됨
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

    public Integer orders(List<ServiceOrderDTO> serviceOrderDTOList, String memberEmail, Integer reservationId) {
        //주문을 했다면 판매하고 있는 상품의 수량을 변경

        Member member = memberRepository.findByEmail(memberEmail);
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(EntityNotFoundException::new);
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
        serviceOrder.setReservation(reservation);
        serviceOrder.setServiceOrderStatus(ServiceOrderStatus.PENDING);
        serviceOrder.setRegDate(LocalDateTime.now());
        serviceOrder.setServiceOrderItemList(serviceOrderItemList);

        serviceOrderRepository.save(serviceOrder);

        return serviceOrder.getServiceOrderId();
    }

    // 구매이력 불러오기
    public Page<ServiceOrderHistoryDTO> getOrderList(String memberEmail, Pageable page) {
        int currentPage = page.getPageNumber() - 1;
        int pageSize = page.getPageSize(); // 클라이언트에서 전달된 size 사용
        // 기존 페이지 그대로 사용
        Pageable pageable = PageRequest.of(page.getPageNumber(), pageSize, Sort.by(Sort.Direction.ASC, "serviceOrderId"));

        // 구매 목록 불러오기
        List<ServiceOrder> serviceOrderList = serviceOrderRepository.findServiceOrders(memberEmail, pageable);

        // 전체 주문 수 카운트
        Integer totalCount = serviceOrderRepository.totalCount(memberEmail);

        List<ServiceOrderHistoryDTO> serviceOrderHistoryDTOList = new ArrayList<>();

        // Entity -> DTO 변환
        for (ServiceOrder serviceOrder : serviceOrderList) {
            ServiceOrderHistoryDTO serviceOrderHistoryDTO = new ServiceOrderHistoryDTO();
            serviceOrderHistoryDTO.setServiceOrderId(serviceOrder.getServiceOrderId());
            serviceOrderHistoryDTO.setRegDate(serviceOrder.getRegDate());
            serviceOrderHistoryDTO.setServiceOrderStatus(serviceOrder.getServiceOrderStatus());
            serviceOrderHistoryDTO.setReservationDTO(modelMapper.map(serviceOrder.getReservation(), ReservationDTO.class));
            serviceOrderHistoryDTO.setMemberDTO(modelMapper.map(serviceOrder.getMember(), MemberDTO.class));

            List<ServiceOrderItem> serviceOrderItemList = serviceOrder.getServiceOrderItemList();
            for (ServiceOrderItem serviceOrderItem : serviceOrderItemList) {
                ServiceOrderItemDTO serviceOrderItemDTO = new ServiceOrderItemDTO();
                serviceOrderItemDTO.setServiceOrderItemId(serviceOrder.getServiceOrderId());
                serviceOrderItemDTO.setServiceMenuName(serviceOrderItem.getServiceMenu().getServiceMenuName());
                serviceOrderItemDTO.setOrderPrice(serviceOrderItem.getOrderPrice());
                serviceOrderItemDTO.setCount(serviceOrderItem.getCount());

                // 메뉴 이미지 경로 세팅
                List<Image> serviceMenuImageList = serviceOrderItem.getServiceMenu().getServiceMenuImageList();
                for (Image image : serviceMenuImageList) {
                    serviceOrderItemDTO.setImagePath(image.getImagePath());
                }
                serviceOrderHistoryDTO.addServiceOrderItemDTO(serviceOrderItemDTO);
            }
            serviceOrderHistoryDTOList.add(serviceOrderHistoryDTO);
        }

        // 'CANCELED' 상태인 주문들을 마지막으로 밀기
        List<ServiceOrderHistoryDTO> sortedList = serviceOrderHistoryDTOList.stream()
                .sorted(Comparator.comparing(serviceOrder -> serviceOrder.getServiceOrderStatus() == ServiceOrderStatus.CANCELED ? 1 : 0))
                .collect(Collectors.toList());

        // PageImpl 생성
        return new PageImpl<>(serviceOrderHistoryDTOList, pageable, totalCount);
    }

    //------------------------------------관리자용-----------------------------------
    //ADMIN의 주문내역관리 페이지
    public Page<ServiceOrderHistoryDTO> adminOrderList(Pageable page, String keyword, String searchType) {
        int currentPage = page.getPageNumber() - 1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "serviceOrderId"));
        Page<ServiceOrder> serviceOrders = null;

        String keywordLike = (keyword != null && !keyword.isEmpty()) ? "%" + keyword + "%" : null;

        if (keywordLike != null) {
            switch (searchType) {
                case "memberName":
                    serviceOrders = serviceOrderRepository.findByMember_MemberNameLike(keywordLike, pageable);
                    break;
                case "memberEmail":
                    serviceOrders = serviceOrderRepository.findByMember_MemberEmailLike(keywordLike, pageable);
                    break;
                case "roomName":
                    serviceOrders = serviceOrderRepository.findByReservation_Room_RoomNameLike(keywordLike, pageable);
                    break;
                case "orderDate":
                    try {
                        LocalDateTime orderDate = LocalDateTime.parse(keyword);
                        serviceOrders = serviceOrderRepository.findByRegDate(orderDate, pageable);
                    } catch (Exception e) {
                        serviceOrders = Page.empty();
                    }
                    break;
            }
        } else {
            serviceOrders = serviceOrderRepository.findAll(pageable);
        }

        return convertToDTOPage(serviceOrders, pageable);
    }
//     CHIEF용 주문 내역 조회
    public Page<ServiceOrderHistoryDTO> chiefOrderList(Pageable page, String keyword, String searchType, Integer memberId) {
        int currentPage = page.getPageNumber() - 1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "serviceOrderId"));
        Page<ServiceOrder> serviceOrders = null;

        String keywordLike = (keyword != null && !keyword.isEmpty()) ? "%" + keyword + "%" : null;

        if (keywordLike != null) {
            switch (searchType) {
                case "memberName":
                    serviceOrders = serviceOrderRepository.findByReservation_Room_HotelId_Company_Member_MemberIdAndMember_MemberNameLike(memberId, keywordLike, pageable);
                    break;
                case "memberEmail":
                    serviceOrders = serviceOrderRepository.findByReservation_Room_HotelId_Company_Member_MemberIdAndMember_MemberEmailLike(memberId, keywordLike, pageable);
                    break;
                case "roomName":
                    serviceOrders = serviceOrderRepository.findByReservation_Room_HotelId_Company_Member_MemberIdAndReservation_Room_RoomNameLike(memberId,keywordLike, pageable);
                    break;
                case "orderDate":
                    try {
                        LocalDateTime orderDate = LocalDateTime.parse(keyword);
                        serviceOrders = serviceOrderRepository.findByReservation_Room_HotelId_Company_Member_MemberIdAndRegDate(memberId, orderDate, pageable);
                    } catch (Exception e) {
                        serviceOrders = Page.empty();
                    }
                    break;
            }
        } else {
            serviceOrders = serviceOrderRepository.findByReservation_Room_HotelId_Company_Member_MemberId(memberId, pageable);
        }

        return convertToDTOPage(serviceOrders, pageable);
    }

    // MANAGER용 주문 내역 조회
    public Page<ServiceOrderHistoryDTO> managerOrderList(Pageable page, String keyword, String searchType, Integer memberId) {
        int currentPage = page.getPageNumber() - 1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "serviceOrderId"));
        Page<ServiceOrder> serviceOrders = null;

        String keywordLike = (keyword != null && !keyword.isEmpty()) ? "%" + keyword + "%" : null;

        if (keywordLike != null) {
            switch (searchType) {
                case "memberName":
                    serviceOrders = serviceOrderRepository.findByReservation_Room_HotelId_Member_MemberIdAndMember_MemberNameLike(memberId, keywordLike, pageable);
                    break;
                case "memberEmail":
                    serviceOrders = serviceOrderRepository.findByReservation_Room_HotelId_Member_MemberIdAndMember_MemberEmailLike( memberId, keywordLike, pageable);
                    break;
                case "roomName":
                    serviceOrders = serviceOrderRepository.findByReservation_Room_HotelId_Member_MemberIdAndReservation_Room_RoomNameLike(memberId, keywordLike, pageable);
                    break;
                case "orderDate":
                    try {
                        LocalDateTime orderDate = LocalDateTime.parse(keyword);
                        serviceOrders = serviceOrderRepository.findByReservation_Room_HotelId_Member_MemberIdAndRegDate(memberId, orderDate,  pageable);
                    } catch (Exception e) {
                        serviceOrders = Page.empty();
                    }
                    break;
            }
        } else {
            serviceOrders = serviceOrderRepository.findByReservation_Room_HotelId_Member_MemberId(memberId, pageable);
        }

        return convertToDTOPage(serviceOrders, pageable);
    }

    private Page<ServiceOrderHistoryDTO> convertToDTOPage(Page<ServiceOrder> serviceOrders, Pageable pageable) {
        List<ServiceOrderHistoryDTO> serviceOrderHistoryDTOList = serviceOrders.stream().map(serviceOrder -> {
            ServiceOrderHistoryDTO serviceOrderHistoryDTO = new ServiceOrderHistoryDTO();
            serviceOrderHistoryDTO.setServiceOrderId(serviceOrder.getServiceOrderId());
            serviceOrderHistoryDTO.setRegDate(serviceOrder.getRegDate());
            serviceOrderHistoryDTO.setServiceOrderStatus(serviceOrder.getServiceOrderStatus());
            serviceOrderHistoryDTO.setReservationDTO(modelMapper.map(serviceOrder.getReservation(), ReservationDTO.class));
            serviceOrderHistoryDTO.setMemberDTO(modelMapper.map(serviceOrder.getMember(), MemberDTO.class));
            serviceOrderHistoryDTO.setServiceOrderItemDtoList(serviceOrder.getServiceOrderItemList().stream().map(item -> {
                ServiceOrderItemDTO itemDTO = new ServiceOrderItemDTO();
                itemDTO.setServiceOrderItemId(item.getServiceOrderItemId());
                itemDTO.setServiceMenuName(item.getServiceMenu().getServiceMenuName());
                itemDTO.setOrderPrice(item.getOrderPrice());
                itemDTO.setCount(item.getCount());
                itemDTO.setImagePath(item.getServiceMenu().getServiceMenuImageList().stream().findFirst().map(Image::getImagePath).orElse(null));
                return itemDTO;
            }).collect(Collectors.toList()));
            return serviceOrderHistoryDTO;
        }).collect(Collectors.toList());

        return new PageImpl<>(serviceOrderHistoryDTOList, pageable, serviceOrders.getTotalElements());
    }



    //주문 상세보기
    public ServiceOrderHistoryDTO getOrderDetail(Integer serviceOrderId) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 없습니다. 주문 ID: " + serviceOrderId));

        ServiceOrderHistoryDTO serviceOrderHistoryDTO = new ServiceOrderHistoryDTO();
        serviceOrderHistoryDTO.setServiceOrderId(serviceOrder.getServiceOrderId());
        serviceOrderHistoryDTO.setRegDate(serviceOrder.getRegDate());
        serviceOrderHistoryDTO.setServiceOrderStatus(serviceOrder.getServiceOrderStatus());
        serviceOrderHistoryDTO.setReservationDTO(modelMapper.map(serviceOrder.getReservation(), ReservationDTO.class));
        serviceOrderHistoryDTO.setMemberDTO(modelMapper.map(serviceOrder.getMember(), MemberDTO.class));

        List<ServiceOrderItemDTO> itemDTOList = new ArrayList<>();
        for (ServiceOrderItem item : serviceOrder.getServiceOrderItemList()) {
            ServiceOrderItemDTO itemDTO = new ServiceOrderItemDTO();
            itemDTO.setServiceOrderItemId(item.getServiceOrderItemId());
            itemDTO.setServiceMenuName(item.getServiceMenu().getServiceMenuName());
            itemDTO.setCount(item.getCount());
            itemDTO.setOrderPrice(item.getOrderPrice());

            // 이미지 추가
            // todo: 이거 이미지처리 목록 뽑았던거랑 비교해서 값 달라지는지 확인할 것
            List<Image> images = item.getServiceMenu().getServiceMenuImageList();
            if (!images.isEmpty()) {
                itemDTO.setImagePath(images.get(0).getImagePath());
            }
            itemDTOList.add(itemDTO);
        }
        serviceOrderHistoryDTO.setServiceOrderItemDtoList(itemDTOList);
        return serviceOrderHistoryDTO;
    }


    // 주문 정보를 수정
    // 주문 정보를 수정
    public void updateOrder(ServiceOrderUpdateDTO updateDTO) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(updateDTO.getServiceOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. ID: " + updateDTO.getServiceOrderId()));

        // 주문 상태 업데이트
        serviceOrder.setServiceOrderStatus(updateDTO.getServiceOrderStatus());

        // 주문 아이템 업데이트
        for (ServiceOrderItemUpdateDTO itemDTO : updateDTO.getOrderItems()) {
            ServiceOrderItem item = serviceOrderItemRepository.findById(itemDTO.getServiceOrderItemId())
                    .orElseThrow(() -> new IllegalArgumentException("주문 아이템을 찾을 수 없습니다. ID: " + itemDTO.getServiceOrderItemId()));

            // 상태가 CANCELED면 가격을 0원으로 설정
            if (updateDTO.getServiceOrderStatus() == ServiceOrderStatus.CANCELED) {
                item.setOrderPrice(0);
            } else {
                // 수량 업데이트
                item.setCount(itemDTO.getCount());

                // 가격 재계산
                item.setOrderPrice(item.getServiceMenu().getServiceMenuPrice() * item.getCount());
            }
        }
    }

    // 주문 삭제
    public void deleteOrder(Integer serviceOrderId) {
        log.info("삭제하려고 서비스까지 들어온 serviceOrderId: " + serviceOrderId);
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. ID: " + serviceOrderId));

        serviceOrderRepository.delete(serviceOrder);
        log.info("삭제 처리 후 serviceOrderId: " + serviceOrderId);
    }

}
