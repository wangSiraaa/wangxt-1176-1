package com.apartment.deposit.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    MERCHANT("MERCHANT", "招商主管"),
    RESIDENT("RESIDENT", "住户"),
    FINANCE("FINANCE", "财务");

    private final String code;
    private final String desc;

    RoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RoleEnum getByCode(String code) {
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}
