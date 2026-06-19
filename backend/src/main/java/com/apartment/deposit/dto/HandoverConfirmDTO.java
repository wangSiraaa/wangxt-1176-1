package com.apartment.deposit.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HandoverConfirmDTO {

    private Long checkinId;

    private BigDecimal meterWater;

    private BigDecimal meterElectric;

    private BigDecimal meterGas;

    private Integer keyCount;

    private Integer doorCardCount;

    private String furnitureCondition;

    private String applianceCondition;

    private String otherCondition;
}
