package com.apartment.deposit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("apt_deduction")
public class AptDeduction implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkinId;

    private String deductionType;

    private String deductionName;

    private BigDecimal amount;

    private String description;

    private String picUrls;

    private Long operatorId;

    private Integer locked;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String reason;

    public String getReason() {
        if (reason != null) return reason;
        return description != null ? description : deductionName;
    }
}
