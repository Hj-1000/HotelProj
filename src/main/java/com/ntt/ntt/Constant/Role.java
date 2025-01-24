package com.ntt.ntt.Constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    ADMIN("관리자"), CHIEF("호텔장"), MANAGER("호텔매니저"), USER("사용자");

    private final String description;
}
