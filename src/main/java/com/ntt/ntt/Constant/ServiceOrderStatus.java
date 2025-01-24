package com.ntt.ntt.Constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceOrderStatus {
    ORDER("주문"), CANCEL("주문취소");

    private final String description;
}
