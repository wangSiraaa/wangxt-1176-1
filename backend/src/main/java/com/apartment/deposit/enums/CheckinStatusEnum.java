package com.apartment.deposit.enums;

import lombok.Getter;

@Getter
public enum CheckinStatusEnum {
    DRAFT("DRAFT", "草稿"),
    PENDING_HANDOVER("PENDING_HANDOVER", "待交接"),
    HANDED_OVER("HANDED_OVER", "已交接"),
    CHECKED_IN("CHECKED_IN", "已入住"),
    SETTLING("SETTLING", "结算中"),
    SETTLED("SETTLED", "已结算"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    CheckinStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
