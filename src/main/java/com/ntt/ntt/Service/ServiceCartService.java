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
    public Integer registerServiceCart(ServiceCartItemDTO serviceCartItemDTO, Integer memberId) {
        log.info("장바구니 서비스로 들어온 멤버의 id" + memberId);
        log.info("장바구니 서비스로 들어온 cartItemDTO"+ serviceCartItemDTO);

        //회원찾기
        Member member =
                memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
        log.info("장바구니 서비스에서 찾은 member" + member);

        //내가 산려고 하는 장바구니 아이템을 장바구니에 넣는다.
        //만약 없다면?
        ServiceMenu serviceMenu =
                serviceMenuRepository.findById(serviceCartItemDTO.getServiceMenuId())
                        .orElseThrow(EntityNotFoundException::new);
        log.info("장바구니서비스에서 찾은 serviceMenu" + serviceMenu);

        ServiceCart serviceCart =
                serviceCartRepository.findByMember_MemberId(member.getMemberId());
        log.info("찾을려는 멤버를 불러온 녀석의 memberId " + member.getMemberId());

        if (serviceCart == null) {
            serviceCart = ServiceCart.createCart(member);
            serviceCartRepository.save(serviceCart);
        }
        // 장바구니가 없으면 만들고 있다면 있는걸로
        // 장바구니 아이템들을 만들어서 넣어주고 저장
        // (매우중요) 이미 장바구니에 동일 상품이 이미 등록되어 있다면
        // 해당 등록된 아이템의 수량증가
        ServiceCartItem savedServiceCartItem =
                serviceCartItemRepository.findByServiceCart_ServiceCartIdAndServiceMenu_ServiceMenuId(serviceCart.getServiceCartId(), serviceMenu.getServiceMenuId());

        // 장바구니에 이미 있다면
        if (savedServiceCartItem != null) {
            //수량증가
            savedServiceCartItem.addCount(serviceCartItemDTO.getCount());
            //저장된 장바구니에서 장바구니 아이템 pk를 반환한다.
            return savedServiceCartItem.getServiceCartItemId();
        } else {//장바구니가 없다면
            ServiceCartItem serviceCartItem =
                    ServiceCartItem.createCartItem(serviceCart, serviceMenu, serviceCartItemDTO.getCount());

            //장바구니에 장바구니 아이템을 저장
            serviceCartItemRepository.save(serviceCartItem);

            return serviceCartItem.getServiceCartItemId();
        }
    }

    public List<ServiceCartDetailDTO> listServiceCart(Integer memberId) {
        // 장바구니의 pk는 1:1 관계이기 때문에 그리고 memberId는 유니크임
        // 멤버 1 장바구니의 pk도 1

        List<ServiceCartDetailDTO> serviceCartDetailDTOList = new ArrayList<>();

        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(EntityNotFoundException::new);

        ServiceCart serviceCart =
                serviceCartRepository.findByMember_MemberId(member.getMemberId());

        if (serviceCart == null) {
            //카트가 존재하지 않으면
            //새로운 리스트인 serviceCartDetailDTOList를 불러온다
            return serviceCartDetailDTOList;
        }
        // 장바구니에 담겨있는 상품 정보를 조회한다
        serviceCartDetailDTOList = serviceCartItemRepository.findByServiceCartDetailDTOList(serviceCart.getServiceCartId());

        return serviceCartDetailDTOList;
    }

    public boolean validateServiceCartItem(Integer serviceCartItemId, Integer memberId) {
        log.info("서비스로 들어온 serviceCartItemId" +serviceCartItemId);
        log.info("서비스로 들어온 memberId" +memberId);
        Member member =
                memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);

        ServiceCartItem serviceCartItem =
                serviceCartItemRepository.findById(serviceCartItemId).orElseThrow(EntityNotFoundException::new);
        if (member != null && serviceCartItem != null) {
            if (!member.getMemberId().equals(serviceCartItem.getServiceCart().getMember().getMemberId())) {
                return false;
            }
        }
        return true;
    }
    //장바구니에서 장바구니 아이템 삭제

    public void deleteServiceCartItem(Integer serviceCartItemId) {
        ServiceCartItem serviceCartItem =
                serviceCartItemRepository.findById(serviceCartItemId)
                        .orElseThrow(EntityNotFoundException::new);
        serviceCartItemRepository.delete(serviceCartItem);
    }

    //장바구니에서 들어온 주문
    public Integer serviceCartItem(List<ServiceCartOrderDTO> serviceCartOrderDTOList, Integer memberId) {
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
                serviceOrderService.orders(serviceOrderDTOList, memberId); //장바구니 아이템을 저장

        for (ServiceCartOrderDTO serviceCartOrderDTO : serviceCartOrderDTOList) {
            ServiceCartItem serviceCartItem =
                    serviceCartItemRepository.findById(serviceCartOrderDTO.getServiceCartItemId()).orElseThrow(EntityNotFoundException::new);

            //장바구니로 주문을 완료했다면 더이상 장바구니 안에 장바구니 아이템이 있으면 안된다
            //그렇지 않으면 계속 장바구니에 아이템이 누적될 것임
            serviceCartItemRepository.delete(serviceCartItem);
        }
        return serviceOrderId;
    }
}
