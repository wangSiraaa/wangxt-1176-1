package com.apartment.deposit.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class DeductionCreateDTO {

    @NotNull(message = "入住单ID不能为空")
    private Long checkinId;

    @NotBlank(message = "扣款类型不能为空")
    private String deductionType;

    private String deductionName;

    @NotNull(message = "扣款金额不能为空")
    private BigDecimal amount;

    private String description;

    private String reason;

    private String picUrls;

    public String getDeductionName() {
        if (deductionName == null && reason != null) {
            return reason.length() > 20 ? reason.substring(0, 20) : reason;
        }
        return deductionName;
    }

    public String getDescription() {
        if (description == null && reason != null) {
            return reason;
        }
        return description;
    }
}
