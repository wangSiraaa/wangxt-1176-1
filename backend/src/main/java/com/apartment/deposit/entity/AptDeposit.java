package com.apartment.deposit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("apt_deposit")
public class AptDeposit implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String depositNo;

    private Long checkinId;

    private Long residentId;

    private Long financeId;

    private String transType;

    private BigDecimal amount;

    private String payMethod;

    private LocalDateTime payTime;

    private String status;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
