package com.apartment.deposit.enums;

import lombok.Getter;

@Getter
public enum RoomStatusEnum {
    VACANT("VACANT", "空置"),
    OCCUPIED("OCCUPIED", "已入住"),
    MAINTENANCE("MAINTENANCE", "维修中");

    private final String code;
    private final String desc;

    RoomStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
