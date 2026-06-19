package com.apartment.deposit.service;

import com.apartment.deposit.entity.SysUser;

import java.util.List;

public interface SysUserService {

    SysUser getByUsername(String username);

    SysUser getById(Long id);

    List<SysUser> listByRole(String roleCode);

    List<SysUser> listAll();
}
