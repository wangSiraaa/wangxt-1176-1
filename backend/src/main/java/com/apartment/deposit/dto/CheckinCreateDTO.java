package com.apartment.deposit.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CheckinCreateDTO {

    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    @NotNull(message = "住户ID不能为空")
    private Long residentId;

    @NotNull(message = "入住日期不能为空")
    private LocalDate checkinDate;

    private LocalDate checkoutDate;

    @NotNull(message = "月租金不能为空")
    private BigDecimal monthlyRent;

    @NotNull(message = "押金金额不能为空")
    private BigDecimal depositAmount;

    private String remark;
}
