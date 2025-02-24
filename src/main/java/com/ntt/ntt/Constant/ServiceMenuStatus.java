package com.ntt.ntt.Constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceMenuStatus {
    SELL("판매중"), SOLD_OUT("매진");

    private final String description;
}
