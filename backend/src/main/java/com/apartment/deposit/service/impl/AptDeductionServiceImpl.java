package com.apartment.deposit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.apartment.deposit.dto.DeductionCreateDTO;
import com.apartment.deposit.entity.AptDeduction;
import com.apartment.deposit.entity.AptSettlement;
import com.apartment.deposit.enums.RoleEnum;
import com.apartment.deposit.exception.BusinessException;
import com.apartment.deposit.mapper.AptDeductionMapper;
import com.apartment.deposit.mapper.AptSettlementMapper;
import com.apartment.deposit.security.SecurityUtil;
import com.apartment.deposit.service.AptDeductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AptDeductionServiceImpl implements AptDeductionService {

    @Autowired
    private AptDeductionMapper aptDeductionMapper;

    @Autowired
    private AptSettlementMapper aptSettlementMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptDeduction create(DeductionCreateDTO dto) {
        if (!SecurityUtil.hasRole(RoleEnum.FINANCE.getCode())
                && !SecurityUtil.hasRole(RoleEnum.MERCHANT.getCode())) {
            throw new BusinessException("无权限添加扣款");
        }

        checkSettlementLocked(dto.getCheckinId());

        AptDeduction deduction = new AptDeduction();
        deduction.setCheckinId(dto.getCheckinId());
        deduction.setDeductionType(dto.getDeductionType());
        deduction.setDeductionName(dto.getDeductionName());
        deduction.setAmount(dto.getAmount());
        deduction.setDescription(dto.getDescription());
        deduction.setPicUrls(dto.getPicUrls());
        deduction.setOperatorId(SecurityUtil.getCurrentUserId());
        deduction.setLocked(0);
        aptDeductionMapper.insert(deduction);
        return deduction;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptDeduction update(Long id, DeductionCreateDTO dto) {
        if (!SecurityUtil.hasRole(RoleEnum.FINANCE.getCode())
                && !SecurityUtil.hasRole(RoleEnum.MERCHANT.getCode())) {
            throw new BusinessException("无权限修改扣款");
        }

        AptDeduction deduction = aptDeductionMapper.selectById(id);
        if (deduction == null) {
            throw new BusinessException("扣款明细不存在");
        }
        if (deduction.getLocked() != null && deduction.getLocked() == 1) {
            throw new BusinessException("该扣款已锁定，无法修改");
        }
        checkSettlementLocked(deduction.getCheckinId());

        deduction.setDeductionType(dto.getDeductionType());
        deduction.setDeductionName(dto.getDeductionName());
        deduction.setAmount(dto.getAmount());
        deduction.setDescription(dto.getDescription());
        deduction.setPicUrls(dto.getPicUrls());
        aptDeductionMapper.updateById(deduction);
        return deduction;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (!SecurityUtil.hasRole(RoleEnum.FINANCE.getCode())
                && !SecurityUtil.hasRole(RoleEnum.MERCHANT.getCode())) {
            throw new BusinessException("无权限删除扣款");
        }

        AptDeduction deduction = aptDeductionMapper.selectById(id);
        if (deduction == null) {
            throw new BusinessException("扣款明细不存在");
        }
        if (deduction.getLocked() != null && deduction.getLocked() == 1) {
            throw new BusinessException("该扣款已锁定，无法删除");
        }
        checkSettlementLocked(deduction.getCheckinId());

        aptDeductionMapper.deleteById(id);
    }

    @Override
    public List<AptDeduction> listByCheckinId(Long checkinId) {
        LambdaQueryWrapper<AptDeduction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptDeduction::getCheckinId, checkinId)
                .orderByDesc(AptDeduction::getCreateTime);
        return aptDeductionMapper.selectList(wrapper);
    }

    @Override
    public AptDeduction getById(Long id) {
        return aptDeductionMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockByCheckinId(Long checkinId) {
        LambdaQueryWrapper<AptDeduction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptDeduction::getCheckinId, checkinId);
        List<AptDeduction> list = aptDeductionMapper.selectList(wrapper);
        for (AptDeduction d : list) {
            d.setLocked(1);
            aptDeductionMapper.updateById(d);
        }
    }

    private void checkSettlementLocked(Long checkinId) {
        LambdaQueryWrapper<AptSettlement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptSettlement::getCheckinId, checkinId)
                .in(AptSettlement::getStatus, "CONFIRMED", "COMPLETED");
        Long count = aptSettlementMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("退租结算已完成，不能修改押金扣款项");
        }
    }
}
