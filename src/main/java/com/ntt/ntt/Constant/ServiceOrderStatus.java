package com.ntt.ntt.Constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceOrderStatus {
    PENDING("보류중"), COMPLETED("완료됨"), CANCELLED("취소됨");

    private final String description;

    // 필요 시, 커스터마이즈할 수 있는 메서드도 추가 가능
    public String getStatus() {
        return this.name(); // 기본적으로 이름을 문자열로 반환
    }
}
