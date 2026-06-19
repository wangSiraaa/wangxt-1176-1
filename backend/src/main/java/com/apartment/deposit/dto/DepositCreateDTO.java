package com.apartment.deposit.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class DepositCreateDTO {

    @NotNull(message = "入住单ID不能为空")
    private Long checkinId;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private String payMethod;

    private String remark;
}
