package com.apartment.deposit.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class SettlementCreateDTO {

    @NotNull(message = "入住单ID不能为空")
    private Long checkinId;

    @NotNull(message = "实际退房日期不能为空")
    private LocalDate actualCheckoutDate;

    private LocalDate checkoutDate;

    private List<Long> deductionIds;

    private String remark;

    private String rentRemark;

    public LocalDate getActualCheckoutDate() {
        if (actualCheckoutDate == null && checkoutDate != null) {
            return checkoutDate;
        }
        return actualCheckoutDate;
    }
}
