package com.ntt.ntt.Service;


import com.ntt.ntt.DTO.ServiceCartDetailDTO;
import com.ntt.ntt.DTO.ServiceCartItemDTO;
import com.ntt.ntt.DTO.ServiceCartOrderDTO;
import com.ntt.ntt.DTO.ServiceOrderDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.ServiceCart;
import com.ntt.ntt.Entity.ServiceCartItem;
import com.ntt.ntt.Entity.ServiceMenu;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.ServiceCartItemRepository;
import com.ntt.ntt.Repository.ServiceCartRepository;
import com.ntt.ntt.Repository.ServiceMenuRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ServiceCartService {

    private final ServiceOrderService serviceOrderService;

    private final ServiceMenuRepository serviceMenuRepository;
    //아이템을 찾아야 해서 findById(serviceMenu.serviceMenuId)

    private final MemberRepository memberRepository;
    //누구의 장바구니인지 찾아야함

    private final ServiceCartRepository serviceCartRepository;
    //장바구니가 있는지 없는지?, 장바구니가 없다면 만들어야함

    private final ServiceCartItemRepository serviceCartItemRepository;
    //장바구니에 넣을 장바구니 아이템을 만들기 위해 serviceCartItemId를 참조해서
    //장바구니 아이템 crud를 이용해야함

    //장바구니 등록
    //장바구니를 따로 만들지 않고 장바구니에 넣을 아이템이 컨트롤러로 들어오면
    //그 값을 가지고 넣을것이고 컨트롤러에서 들어오는 memberId를 통해서 멤버를 찾는다.
    public Integer registerServiceCart(ServiceCartItemDTO serviceCartItemDTO, String memberEmail) {
        log.info("장바구니 서비스로 들어온 멤버의 email" + memberEmail);
        log.info("장바구니 서비스로 들어온 cartItemDTO" + serviceCartItemDTO);

        //회원찾기
        Member member = memberRepository.findByEmail(memberEmail);
        log.info("장바구니 서비스에서 찾은 member" + member);

        //내가 산려고 하는 장바구니 아이템을 장바구니에 넣는다.
        ServiceMenu serviceMenu = serviceMenuRepository.findById(serviceCartItemDTO.getServiceMenuId())
                .orElseThrow(EntityNotFoundException::new);
        log.info("장바구니 서비스에서 찾은 serviceMenu" + serviceMenu);

        // 장바구니 찾기
        ServiceCart serviceCart = serviceCartRepository.findByMember_MemberId(member.getMemberId());
        log.info("찾을려는 멤버를 불러온 녀석의 memberId " + member.getMemberId());

        if (serviceCart == null) {
            serviceCart = ServiceCart.createCart(member);
            serviceCartRepository.save(serviceCart);
        }

        // 장바구니 아이템이 이미 존재하는지 확인
        ServiceCartItem savedServiceCartItem =
                serviceCartItemRepository.findByServiceCart_ServiceCartIdAndServiceMenu_ServiceMenuId(serviceCart.getServiceCartId(), serviceMenu.getServiceMenuId());

        // 장바구니에 이미 해당 메뉴가 있다면
        if (savedServiceCartItem != null) {
            // 수량 증가
            savedServiceCartItem.addCount(serviceCartItemDTO.getCount());

            // 변경된 장바구니 아이템을 DB에 저장
            serviceCartItemRepository.save(savedServiceCartItem); // DB에 반영

            return savedServiceCartItem.getServiceCartItemId();
        } else {  // 장바구니에 해당 메뉴가 없다면
            ServiceCartItem serviceCartItem = ServiceCartItem.createCartItem(serviceCart, serviceMenu, serviceCartItemDTO.getCount());
            serviceCartItemRepository.save(serviceCartItem);
            return serviceCartItem.getServiceCartItemId();
        }
    }


//    public List<ServiceCartDetailDTO> listServiceCart(String memberEmail) {
//        // 장바구니의 pk는 1:1 관계이기 때문에 그리고 memberId는 유니크임
//        // 멤버 1 장바구니의 pk도 1
//
//        List<ServiceCartDetailDTO> serviceCartDetailDTOList = new ArrayList<>();
//
//        Member member =
//                memberRepository.findByEmail(memberEmail);
//
//
//        ServiceCart serviceCart =
//                serviceCartRepository.findByMember_MemberEmail(memberEmail);
//
//        if (serviceCart == null) {
//            //카트가 존재하지 않으면
//            //새로운 리스트인 serviceCartDetailDTOList를 불러온다
//            return serviceCartDetailDTOList;
//        }
//        // 장바구니에 담겨있는 상품 정보를 조회한다
//        serviceCartDetailDTOList = serviceCartItemRepository.findByServiceCartDetailDTOList(serviceCart.getServiceCartId());
//
//        return serviceCartDetailDTOList;
//    }

    public List<ServiceCartDetailDTO> listServiceCart(String memberEmail) {
        List<ServiceCartDetailDTO> serviceCartDetailDTOList = new ArrayList<>();

        Member member = memberRepository.findByEmail(memberEmail);
        ServiceCart serviceCart = serviceCartRepository.findByMember_MemberEmail(member.getMemberEmail());

        if (serviceCart == null) {
            return serviceCartDetailDTOList;
        }

        // DTO에 serviceMenuId 포함하도록 수정된 쿼리 사용
        serviceCartDetailDTOList = serviceCartItemRepository.findByServiceCartDetailDTOList(serviceCart.getServiceCartId());

        return serviceCartDetailDTOList;
    }


    public boolean validateServiceCartItem(Integer serviceCartItemId, String memberEmail) {
        log.info("서비스로 들어온 serviceCartItemId: " + serviceCartItemId);
        log.info("서비스로 들어온 memberEmail: " + memberEmail);

        // 이메일을 통해 멤버 찾기
        Member member = memberRepository.findByEmail(memberEmail);
        if (member == null) {
            log.info("해당 이메일을 가진 회원이 존재하지 않습니다: " + memberEmail);
            return false;  // 회원이 존재하지 않으면 바로 false 반환
        }

        // serviceCartItemId로 장바구니 아이템 찾기
        ServiceCartItem serviceCartItem = serviceCartItemRepository.findById(serviceCartItemId)
                .orElseThrow(() -> new EntityNotFoundException("장바구니 아이템을 찾을 수 없습니다. ID: " + serviceCartItemId));

        // 멤버가 해당 장바구니 아이템을 소유하고 있는지 확인
        if (!member.getMemberEmail().equals(serviceCartItem.getServiceCart().getMember().getMemberEmail())) {
            log.info("회원이 해당 장바구니 아이템을 소유하고 있지 않습니다. memberEmail: " + member.getMemberEmail()
                    + ", serviceCartItemId: " + serviceCartItemId);
            return false;  // 소유하지 않으면 false 반환
        }

        return true;  // 모든 검사를 통과하면 true 반환
    }
    //장바구니에서 장바구니 아이템 삭제

    public void deleteServiceCartItem(Integer serviceCartItemId) {
        ServiceCartItem serviceCartItem =
                serviceCartItemRepository.findById(serviceCartItemId)
                        .orElseThrow(EntityNotFoundException::new);
        serviceCartItemRepository.delete(serviceCartItem);
    }

    //장바구니에서 들어온 주문
    public Integer orderServiceCartItem(List<ServiceCartOrderDTO> serviceCartOrderDTOList, String memberEmail) {
        //serviceCartOrderDTOList에는 serviceCartItemId가 들어있음

        List<ServiceOrderDTO> serviceOrderDTOList = new ArrayList<>();

        for (ServiceCartOrderDTO serviceCartOrderDTO : serviceCartOrderDTOList) {
            //serviceCartItemId를 하나씩 가지고
            ServiceCartItem serviceCartItem =
                    serviceCartItemRepository.findById(serviceCartOrderDTO.getServiceCartItemId())
                            .orElseThrow(EntityNotFoundException::new);

            ServiceOrderDTO serviceOrderDTO = new ServiceOrderDTO();
            serviceOrderDTO.setServiceMenuId(serviceCartItem.getServiceMenu().getServiceMenuId());
            serviceOrderDTO.setCount(serviceCartItem.getCount());

            serviceOrderDTOList.add(serviceOrderDTO);
        }
        Integer serviceOrderId =
                serviceOrderService.orders(serviceOrderDTOList, memberEmail); //장바구니 아이템을 저장

        for (ServiceCartOrderDTO serviceCartOrderDTO : serviceCartOrderDTOList) {
            ServiceCartItem serviceCartItem =
                    serviceCartItemRepository.findById(serviceCartOrderDTO.getServiceCartItemId())
                            .orElseThrow(EntityNotFoundException::new);

            //장바구니로 주문을 완료했다면 더이상 장바구니 안에 장바구니 아이템이 있으면 안된다
            //그렇지 않으면 계속 장바구니에 아이템이 누적될 것임
            serviceCartItemRepository.delete(serviceCartItem);
        }
        return serviceOrderId;
    }
}
