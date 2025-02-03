package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.ServiceCartDTO;
import com.ntt.ntt.DTO.ServiceCartItemDTO;
import com.ntt.ntt.Entity.ServiceCart;
import com.ntt.ntt.Entity.ServiceCartItem;
import com.ntt.ntt.Entity.ServiceMenu;
import com.ntt.ntt.Repository.ServiceCartItemRepository;
import com.ntt.ntt.Repository.ServiceCartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ServiceCartService {
    private final ServiceCartRepository serviceCartRepository;
    private final ServiceCartItemRepository serviceCartItemRepository;
    private final ModelMapper modelMapper;

    // 특정 회원의 장바구니 조회
    public ServiceCartDTO getCartByMemberId(Integer memberId) {
        ServiceCart serviceCart = serviceCartRepository.findCartByMemberId(memberId);
        if (serviceCart == null) {
            throw new RuntimeException("장바구니가 존재하지 않습니다.");
        }
        return modelMapper.map(serviceCart, ServiceCartDTO.class);
    }

    // 장바구니에 아이템 추가
    public void addItemToCart(Integer serviceCartId, ServiceCartItemDTO serviceCartItemDTO) {
        ServiceCart serviceCart = serviceCartRepository.findById(serviceCartId)
                .orElseThrow(() -> new RuntimeException("장바구니를 찾을 수 없습니다."));

        ServiceMenu serviceMenu = new ServiceMenu(); // 메뉴 ID로 서비스 메뉴를 찾거나, 다른 방식으로 가져와야 할 수 있음.
        serviceMenu.setServiceMenuId(serviceCartItemDTO.getServiceMenuId());

        ServiceCartItem serviceCartItem = modelMapper.map(serviceCartItemDTO, ServiceCartItem.class);
        serviceCartItem.setServiceCart(serviceCart);
        serviceCartItem.setServiceMenu(serviceMenu);

        serviceCartItemRepository.save(serviceCartItem);
    }

    // 장바구니 전체 비우기
    public void clearCart(Integer memberId) {
        ServiceCart serviceCart = serviceCartRepository.findByMember_MemberId(memberId);
        if (serviceCart == null) {
            throw new RuntimeException("장바구니가 존재하지 않습니다.");
        }
        serviceCartItemRepository.deleteAll(serviceCart.getServiceCartItemList());
    }

    // 장바구니 아이템 수량 변경
    public void updateCartItemQuantity(Integer serviceCartItemId, Integer newCount) {
        ServiceCartItem serviceCartItem = serviceCartItemRepository.findById(serviceCartItemId)
                .orElseThrow(() -> new RuntimeException("장바구니 아이템을 찾을 수 없습니다."));
        serviceCartItem.setCount(newCount);
        serviceCartItemRepository.save(serviceCartItem);
    }


    // 장바구니 아이템 삭제
    public void removeItemFromCart(Integer serviceCartItemId) {
        serviceCartItemRepository.deleteById(serviceCartItemId);
    }
}

