package com.apartment.deposit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("apt_settlement")
public class AptSettlement implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String settlementNo;

    private Long checkinId;

    private Long residentId;

    private Long financeId;

    private Long merchantId;

    private LocalDate actualCheckoutDate;

    private BigDecimal totalRent;

    private BigDecimal paidRent;

    private BigDecimal remainingRent;

    private BigDecimal utilityFee;

    private BigDecimal totalDeposit;

    private BigDecimal totalDeduction;

    private BigDecimal refundAmount;

    private BigDecimal payableAmount;

    private String status;

    private LocalDateTime settlementTime;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String checkinNo;

    @TableField(exist = false)
    private Long roomId;

    @TableField(exist = false)
    private LocalDate actualCheckinDate;

    @TableField(exist = false)
    private Long rentMonths;

    @TableField(exist = false)
    private BigDecimal totalPaid;

    @TableField(exist = false)
    private BigDecimal unpaidAmount;
}
