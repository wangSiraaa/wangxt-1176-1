package com.apartment.deposit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.apartment.deposit.dto.LoginDTO;
import com.apartment.deposit.dto.LoginVO;
import com.apartment.deposit.entity.SysUser;
import com.apartment.deposit.enums.RoleEnum;
import com.apartment.deposit.exception.BusinessException;
import com.apartment.deposit.security.JwtTokenUtil;
import com.apartment.deposit.service.AuthService;
import com.apartment.deposit.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = sysUserService.getByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getRoleCode());
        Date expireDate = jwtTokenUtil.getExpirationDateFromToken(token);

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRoleCode(user.getRoleCode());
        RoleEnum roleEnum = RoleEnum.getByCode(user.getRoleCode());
        vo.setRoleDesc(roleEnum != null ? roleEnum.getDesc() : "");
        vo.setToken(token);
        vo.setExpireTime(LocalDateTime.ofInstant(expireDate.toInstant(), ZoneId.systemDefault()));

        return vo;
    }
}
