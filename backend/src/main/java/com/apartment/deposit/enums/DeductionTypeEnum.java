package com.apartment.deposit.enums;

import lombok.Getter;

@Getter
public enum DeductionTypeEnum {
    DAMAGE("DAMAGE", "物品损坏"),
    CLEAN("CLEAN", "清洁费"),
    UTILITY("UTILITY", "水电费"),
    OVERDUE("OVERDUE", "逾期费用"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;

    DeductionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
