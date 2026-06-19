package com.apartment.deposit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("apt_handover")
public class AptHandover implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkinId;

    private String handoverType;

    private LocalDateTime handoverTime;

    private Long residentId;

    private Long merchantId;

    private BigDecimal meterWater;

    private BigDecimal meterElectric;

    private BigDecimal meterGas;

    private Integer keyCount;

    private Integer doorCardCount;

    private String furnitureCondition;

    private String applianceCondition;

    private String otherCondition;

    private Integer residentSign;

    private Integer merchantSign;

    private Integer confirmed;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
