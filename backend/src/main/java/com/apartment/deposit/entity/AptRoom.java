package com.apartment.deposit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("apt_room")
public class AptRoom implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roomNo;

    private String building;

    private Integer floor;

    private String roomType;

    private BigDecimal area;

    private BigDecimal monthlyRent;

    private BigDecimal depositAmount;

    private String status;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
