package com.apartment.deposit.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DisputeCreateDTO {

    @NotNull(message = "入住单ID不能为空")
    private Long checkinId;

    private Long deductionId;

    @NotBlank(message = "争议类型不能为空")
    private String disputeType;

    @NotBlank(message = "争议内容不能为空")
    private String content;

    private String evidenceUrls;
}
