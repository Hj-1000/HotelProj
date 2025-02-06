package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.ServiceCartItemDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.ServiceCartItem;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.ServiceCartItemRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ServiceCartItemService {
    private final ServiceCartItemRepository serviceCartItemRepository;
    private final MemberRepository memberRepository;

    //카트에 담긴 카트아이템의 수량을 변경
    //카트 id는 필요 없다 // 카트 아이템이 자식이므로 직접 pk 값을 알고 있기 때문

    public Integer updateServiceCartItemCount(ServiceCartItemDTO serviceCartItemDTO, Integer memberId) throws Exception {
        // 내 카트가 맞는지 확인
        Member member =
                memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);

        //카트 아이디를 받아서 로그인한 회원정보와 비교후에
        //카트아이템 아이디만 있음

        //컨트롤러에서 받은 CartItemDTO에 카트아이템 아이디로

        ServiceCartItem serviceCartItem =
                serviceCartItemRepository.findById(serviceCartItemDTO.getServiceCartItemId())
                        .orElseThrow(EntityNotFoundException::new);

        if (serviceCartItem.getServiceCart() != null && member != null && serviceCartItem.getServiceCart().getMember().getMemberId() != member.getMemberId()) {
            throw new Exception();
        }
        serviceCartItem.setCount(serviceCartItemDTO.getCount());
        return serviceCartItem.getServiceCartItemId();
    }
}
