package com.apartment.deposit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("apt_checkin")
public class AptCheckin implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String checkinNo;

    private Long roomId;

    private Long residentId;

    private Long merchantId;

    private LocalDate checkinDate;

    private LocalDate checkoutDate;

    private LocalDate actualCheckinDate;

    private LocalDate actualCheckoutDate;

    private BigDecimal monthlyRent;

    private BigDecimal depositAmount;

    private String status;

    private Integer handoverConfirmed;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
