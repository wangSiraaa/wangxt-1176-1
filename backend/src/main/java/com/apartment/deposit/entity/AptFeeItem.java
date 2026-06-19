package com.apartment.deposit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("apt_fee_item")
public class AptFeeItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkinId;

    private String feeType;

    private String feeName;

    private String feeMonth;

    private BigDecimal amount;

    private Integer paid;

    private LocalDateTime paidTime;

    private Long operatorId;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
