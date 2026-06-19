package com.apartment.deposit.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DisputeReviewDTO {

    private String reviewOpinion;

    private BigDecimal adjustedAmount;

    private String status;
}
