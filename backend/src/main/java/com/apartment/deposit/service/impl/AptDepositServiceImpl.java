package com.apartment.deposit.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.apartment.deposit.dto.DepositCreateDTO;
import com.apartment.deposit.entity.AptCheckin;
import com.apartment.deposit.entity.AptDeposit;
import com.apartment.deposit.entity.AptFeeItem;
import com.apartment.deposit.entity.AptSettlement;
import com.apartment.deposit.enums.CheckinStatusEnum;
import com.apartment.deposit.enums.DepositTransTypeEnum;
import com.apartment.deposit.enums.RoleEnum;
import com.apartment.deposit.exception.BusinessException;
import com.apartment.deposit.mapper.AptCheckinMapper;
import com.apartment.deposit.mapper.AptDepositMapper;
import com.apartment.deposit.mapper.AptFeeItemMapper;
import com.apartment.deposit.mapper.AptSettlementMapper;
import com.apartment.deposit.security.SecurityUtil;
import com.apartment.deposit.service.AptDepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AptDepositServiceImpl implements AptDepositService {

    @Autowired
    private AptDepositMapper aptDepositMapper;

    @Autowired
    private AptCheckinMapper aptCheckinMapper;

    @Autowired
    private AptFeeItemMapper aptFeeItemMapper;

    @Autowired
    private AptSettlementMapper aptSettlementMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptDeposit collectDeposit(DepositCreateDTO dto) {
        if (!SecurityUtil.hasRole(RoleEnum.FINANCE.getCode())) {
            throw new BusinessException("只有财务可以收取押金");
        }

        AptCheckin checkin = validateCheckin(dto.getCheckinId());

        AptDeposit deposit = new AptDeposit();
        deposit.setDepositNo(genDepositNo("YS"));
        deposit.setCheckinId(dto.getCheckinId());
        deposit.setResidentId(checkin.getResidentId());
        deposit.setFinanceId(SecurityUtil.getCurrentUserId());
        deposit.setTransType(DepositTransTypeEnum.COLLECT.getCode());
        deposit.setAmount(dto.getAmount());
        deposit.setPayMethod(dto.getPayMethod());
        deposit.setPayTime(LocalDateTime.now());
        deposit.setStatus("COMPLETED");
        deposit.setRemark(dto.getRemark());
        aptDepositMapper.insert(deposit);
        return deposit;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptDeposit refundDeposit(DepositCreateDTO dto) {
        if (!SecurityUtil.hasRole(RoleEnum.FINANCE.getCode())) {
            throw new BusinessException("只有财务可以退还押金");
        }

        AptCheckin checkin = validateCheckin(dto.getCheckinId());

        if (!CheckinStatusEnum.SETTLING.getCode().equals(checkin.getStatus())
                && !CheckinStatusEnum.SETTLED.getCode().equals(checkin.getStatus())) {
            throw new BusinessException("当前状态不能退还押金，需要先发起结算");
        }

        List<AptFeeItem> unpaidFees = getUnpaidFees(dto.getCheckinId());
        if (!unpaidFees.isEmpty()) {
            throw new BusinessException("存在未结清的费用，不能退还押金");
        }

        BigDecimal collected = getTotalAmount(dto.getCheckinId(), DepositTransTypeEnum.COLLECT.getCode());
        BigDecimal refunded = getTotalAmount(dto.getCheckinId(), DepositTransTypeEnum.REFUND.getCode());
        BigDecimal available = collected.subtract(refunded);

        AptSettlement settlement = getSettlementByCheckinId(dto.getCheckinId());
        if (settlement != null && settlement.getTotalDeduction() != null) {
            available = available.subtract(settlement.getTotalDeduction());
        }

        if (available.compareTo(dto.getAmount()) < 0) {
            throw new BusinessException("可退还押金金额不足，当前可退：" + available);
        }

        AptDeposit deposit = new AptDeposit();
        deposit.setDepositNo(genDepositNo("TK"));
        deposit.setCheckinId(dto.getCheckinId());
        deposit.setResidentId(checkin.getResidentId());
        deposit.setFinanceId(SecurityUtil.getCurrentUserId());
        deposit.setTransType(DepositTransTypeEnum.REFUND.getCode());
        deposit.setAmount(dto.getAmount());
        deposit.setPayMethod(dto.getPayMethod());
        deposit.setPayTime(LocalDateTime.now());
        deposit.setStatus("COMPLETED");
        deposit.setRemark(dto.getRemark());
        aptDepositMapper.insert(deposit);
        return deposit;
    }

    private AptSettlement getSettlementByCheckinId(Long checkinId) {
        LambdaQueryWrapper<AptSettlement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptSettlement::getCheckinId, checkinId);
        return aptSettlementMapper.selectOne(wrapper);
    }

    private List<AptFeeItem> getUnpaidFees(Long checkinId) {
        LambdaQueryWrapper<AptFeeItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptFeeItem::getCheckinId, checkinId)
                .eq(AptFeeItem::getPaid, 0);
        return aptFeeItemMapper.selectList(wrapper);
    }

    private BigDecimal getTotalAmount(Long checkinId, String transType) {
        LambdaQueryWrapper<AptDeposit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptDeposit::getCheckinId, checkinId)
                .eq(AptDeposit::getTransType, transType)
                .eq(AptDeposit::getStatus, "COMPLETED");
        List<AptDeposit> list = aptDepositMapper.selectList(wrapper);
        return list.stream()
                .map(AptDeposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String genDepositNo(String prefix) {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private AptCheckin validateCheckin(Long checkinId) {
        AptCheckin checkin = aptCheckinMapper.selectById(checkinId);
        if (checkin == null) {
            throw new BusinessException("入住单不存在");
        }
        return checkin;
    }

    @Override
    public List<AptDeposit> listByCheckinId(Long checkinId) {
        LambdaQueryWrapper<AptDeposit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptDeposit::getCheckinId, checkinId)
                .orderByDesc(AptDeposit::getCreateTime);
        return aptDepositMapper.selectList(wrapper);
    }

    @Override
    public AptDeposit getById(Long id) {
        return aptDepositMapper.selectById(id);
    }
}
