package com.apartment.deposit.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginVO {
    private Long userId;
    private String username;
    private String realName;
    private String roleCode;
    private String roleDesc;
    private String token;
    private LocalDateTime expireTime;
}
