package com.apartment.deposit.controller;

import com.apartment.deposit.common.Result;
import com.apartment.deposit.entity.SysUser;
import com.apartment.deposit.security.SecurityUtil;
import com.apartment.deposit.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/current")
    public Result<SysUser> getCurrentUser() {
        return Result.success(SecurityUtil.getCurrentUser());
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        return Result.success(sysUserService.listAll());
    }

    @GetMapping("/listByRole")
    public Result<List<SysUser>> listByRole(@RequestParam String roleCode) {
        return Result.success(sysUserService.listByRole(roleCode));
    }
}
