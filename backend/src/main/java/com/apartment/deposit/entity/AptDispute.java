package com.apartment.deposit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("apt_dispute")
public class AptDispute implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkinId;

    private Long deductionId;

    private Long residentId;

    private String disputeType;

    private String content;

    private String evidenceUrls;

    private String status;

    private Long reviewerId;

    private String reviewOpinion;

    private BigDecimal adjustedAmount;

    private LocalDateTime reviewTime;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String deductionType;

    @TableField(exist = false)
    private String deductionDescription;

    @TableField(exist = false)
    private BigDecimal deductionAmount;
}
