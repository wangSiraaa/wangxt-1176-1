package com.apartment.deposit.enums;

import lombok.Getter;

@Getter
public enum DepositTransTypeEnum {
    COLLECT("COLLECT", "收取"),
    REFUND("REFUND", "退还");

    private final String code;
    private final String desc;

    DepositTransTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
