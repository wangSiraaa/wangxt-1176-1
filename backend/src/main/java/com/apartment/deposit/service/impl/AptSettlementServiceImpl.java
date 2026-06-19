package com.apartment.deposit.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.apartment.deposit.dto.SettlementCreateDTO;
import com.apartment.deposit.entity.*;
import com.apartment.deposit.enums.CheckinStatusEnum;
import com.apartment.deposit.enums.RoleEnum;
import com.apartment.deposit.enums.RoomStatusEnum;
import com.apartment.deposit.exception.BusinessException;
import com.apartment.deposit.mapper.*;
import com.apartment.deposit.security.SecurityUtil;
import com.apartment.deposit.service.AptDeductionService;
import com.apartment.deposit.service.AptSettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AptSettlementServiceImpl implements AptSettlementService {

    @Autowired
    private AptSettlementMapper aptSettlementMapper;

    @Autowired
    private AptCheckinMapper aptCheckinMapper;

    @Autowired
    private AptRoomMapper aptRoomMapper;

    @Autowired
    private AptDeductionMapper aptDeductionMapper;

    @Autowired
    private AptDepositMapper aptDepositMapper;

    @Autowired
    private AptFeeItemMapper aptFeeItemMapper;

    @Autowired
    private AptDeductionService aptDeductionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptSettlement create(SettlementCreateDTO dto) {
        if (!SecurityUtil.hasRole(RoleEnum.FINANCE.getCode())) {
            throw new BusinessException("只有财务可以创建结算单");
        }

        AptCheckin checkin = aptCheckinMapper.selectById(dto.getCheckinId());
        if (checkin == null) {
            throw new BusinessException("入住单不存在");
        }
        if (!CheckinStatusEnum.CHECKED_IN.getCode().equals(checkin.getStatus())
                && !CheckinStatusEnum.SETTLING.getCode().equals(checkin.getStatus())) {
            throw new BusinessException("当前状态不能创建结算");
        }

        AptSettlement exist = getByCheckinId(dto.getCheckinId());
        if (exist != null && !"CANCELLED".equals(exist.getStatus())) {
            throw new BusinessException("该入住单已存在结算单");
        }

        checkin.setStatus(CheckinStatusEnum.SETTLING.getCode());
        aptCheckinMapper.updateById(checkin);

        LocalDate startDate = checkin.getActualCheckinDate() != null
                ? checkin.getActualCheckinDate() : checkin.getCheckinDate();
        LocalDate endDate = dto.getActualCheckoutDate();
        long months = ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), endDate.withDayOfMonth(1));
        if (endDate.getDayOfMonth() > startDate.getDayOfMonth()) {
            months++;
        }
        if (months <= 0) months = 1;

        BigDecimal totalRent = checkin.getMonthlyRent().multiply(BigDecimal.valueOf(months));
        BigDecimal paidRent = getPaidRent(dto.getCheckinId());
        BigDecimal remainingRent = totalRent.subtract(paidRent);
        if (remainingRent.compareTo(BigDecimal.ZERO) < 0) remainingRent = BigDecimal.ZERO;

        BigDecimal utilityFee = getUnpaidUtilityFee(dto.getCheckinId());

        BigDecimal totalDeposit = getTotalDeposit(dto.getCheckinId());
        BigDecimal totalDeduction = getTotalDeduction(dto.getCheckinId());

        BigDecimal refundAmount = totalDeposit.subtract(totalDeduction).subtract(remainingRent).subtract(utilityFee);
        BigDecimal payableAmount = BigDecimal.ZERO;
        if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
            payableAmount = refundAmount.negate();
            refundAmount = BigDecimal.ZERO;
        }

        AptSettlement settlement = new AptSettlement();
        settlement.setSettlementNo(genSettlementNo());
        settlement.setCheckinId(dto.getCheckinId());
        settlement.setResidentId(checkin.getResidentId());
        settlement.setFinanceId(SecurityUtil.getCurrentUserId());
        settlement.setMerchantId(checkin.getMerchantId());
        settlement.setActualCheckoutDate(dto.getActualCheckoutDate());
        settlement.setTotalRent(totalRent);
        settlement.setPaidRent(paidRent);
        settlement.setRemainingRent(remainingRent);
        settlement.setUtilityFee(utilityFee);
        settlement.setTotalDeposit(totalDeposit);
        settlement.setTotalDeduction(totalDeduction);
        settlement.setRefundAmount(refundAmount);
        settlement.setPayableAmount(payableAmount);
        settlement.setStatus("DRAFT");
        settlement.setRemark(dto.getRemark());
        aptSettlementMapper.insert(settlement);
        return settlement;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptSettlement confirm(Long id) {
        if (!SecurityUtil.hasRole(RoleEnum.FINANCE.getCode())) {
            throw new BusinessException("只有财务可以确认结算");
        }

        AptSettlement settlement = getAndValidate(id);
        if (!"DRAFT".equals(settlement.getStatus())) {
            throw new BusinessException("当前状态不能确认");
        }
        settlement.setStatus("CONFIRMED");
        aptDeductionService.lockByCheckinId(settlement.getCheckinId());
        aptSettlementMapper.updateById(settlement);
        return settlement;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptSettlement complete(Long id) {
        if (!SecurityUtil.hasRole(RoleEnum.FINANCE.getCode())) {
            throw new BusinessException("只有财务可以完成结算");
        }

        AptSettlement settlement = getAndValidate(id);
        if (!"CONFIRMED".equals(settlement.getStatus())) {
            throw new BusinessException("当前状态不能完成结算");
        }

        List<AptFeeItem> unpaidFees = getUnpaidFees(settlement.getCheckinId());
        if (!unpaidFees.isEmpty() && settlement.getPayableAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("存在未结清费用，请先确认住户已补缴");
        }

        settlement.setStatus("COMPLETED");
        settlement.setSettlementTime(LocalDateTime.now());
        aptSettlementMapper.updateById(settlement);

        AptCheckin checkin = aptCheckinMapper.selectById(settlement.getCheckinId());
        if (checkin != null) {
            checkin.setStatus(CheckinStatusEnum.SETTLED.getCode());
            checkin.setActualCheckoutDate(settlement.getActualCheckoutDate());
            aptCheckinMapper.updateById(checkin);

            AptRoom room = aptRoomMapper.selectById(checkin.getRoomId());
            if (room != null) {
                room.setStatus(RoomStatusEnum.VACANT.getCode());
                aptRoomMapper.updateById(room);
            }
        }

        return settlement;
    }

    private AptSettlement getAndValidate(Long id) {
        AptSettlement settlement = aptSettlementMapper.selectById(id);
        if (settlement == null) {
            throw new BusinessException("结算单不存在");
        }
        return settlement;
    }

    @Override
    public AptSettlement getById(Long id) {
        AptSettlement settlement = aptSettlementMapper.selectById(id);
        if (settlement != null) {
            fillExtraFields(settlement);
        }
        return settlement;
    }

    @Override
    public AptSettlement getByCheckinId(Long checkinId) {
        LambdaQueryWrapper<AptSettlement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptSettlement::getCheckinId, checkinId);
        AptSettlement settlement = aptSettlementMapper.selectOne(wrapper);
        if (settlement != null) {
            fillExtraFields(settlement);
        }
        return settlement;
    }

    @Override
    public List<AptSettlement> list(String status) {
        LambdaQueryWrapper<AptSettlement> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(AptSettlement::getStatus, status);
        }
        wrapper.orderByDesc(AptSettlement::getCreateTime);
        List<AptSettlement> list = aptSettlementMapper.selectList(wrapper);
        for (AptSettlement s : list) {
            fillExtraFields(s);
        }
        return list;
    }

    private void fillExtraFields(AptSettlement settlement) {
        AptCheckin checkin = aptCheckinMapper.selectById(settlement.getCheckinId());
        if (checkin != null) {
            settlement.setCheckinNo(checkin.getCheckinNo());
            settlement.setRoomId(checkin.getRoomId());
            settlement.setResidentId(checkin.getResidentId());
            settlement.setActualCheckinDate(checkin.getActualCheckinDate() != null
                    ? checkin.getActualCheckinDate() : checkin.getCheckinDate());

            LocalDate start = settlement.getActualCheckinDate();
            LocalDate end = settlement.getActualCheckoutDate();
            if (start != null && end != null) {
                long months = ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1));
                if (end.getDayOfMonth() > start.getDayOfMonth()) months++;
                if (months <= 0) months = 1;
                settlement.setRentMonths(months);
            }
        }
        settlement.setTotalPaid(settlement.getPaidRent());
        BigDecimal unpaid = (settlement.getRemainingRent() != null ? settlement.getRemainingRent() : BigDecimal.ZERO)
                .add(settlement.getUtilityFee() != null ? settlement.getUtilityFee() : BigDecimal.ZERO);
        settlement.setUnpaidAmount(unpaid);
    }

    private BigDecimal getPaidRent(Long checkinId) {
        LambdaQueryWrapper<AptFeeItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptFeeItem::getCheckinId, checkinId)
                .eq(AptFeeItem::getFeeType, "RENT")
                .eq(AptFeeItem::getPaid, 1);
        List<AptFeeItem> list = aptFeeItemMapper.selectList(wrapper);
        return list.stream().map(AptFeeItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getUnpaidUtilityFee(Long checkinId) {
        LambdaQueryWrapper<AptFeeItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptFeeItem::getCheckinId, checkinId)
                .in(AptFeeItem::getFeeType, "WATER", "ELECTRIC", "GAS", "OTHER")
                .eq(AptFeeItem::getPaid, 0);
        List<AptFeeItem> list = aptFeeItemMapper.selectList(wrapper);
        return list.stream().map(AptFeeItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<AptFeeItem> getUnpaidFees(Long checkinId) {
        LambdaQueryWrapper<AptFeeItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptFeeItem::getCheckinId, checkinId)
                .eq(AptFeeItem::getPaid, 0);
        return aptFeeItemMapper.selectList(wrapper);
    }

    private BigDecimal getTotalDeposit(Long checkinId) {
        LambdaQueryWrapper<AptDeposit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptDeposit::getCheckinId, checkinId)
                .eq(AptDeposit::getTransType, "COLLECT")
                .eq(AptDeposit::getStatus, "COMPLETED");
        List<AptDeposit> list = aptDepositMapper.selectList(wrapper);
        BigDecimal collected = list.stream().map(AptDeposit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        LambdaQueryWrapper<AptDeposit> refundWrapper = new LambdaQueryWrapper<>();
        refundWrapper.eq(AptDeposit::getCheckinId, checkinId)
                .eq(AptDeposit::getTransType, "REFUND")
                .eq(AptDeposit::getStatus, "COMPLETED");
        List<AptDeposit> refundList = aptDepositMapper.selectList(refundWrapper);
        BigDecimal refunded = refundList.stream().map(AptDeposit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return collected.subtract(refunded);
    }

    private BigDecimal getTotalDeduction(Long checkinId) {
        LambdaQueryWrapper<AptDeduction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptDeduction::getCheckinId, checkinId);
        List<AptDeduction> list = aptDeductionMapper.selectList(wrapper);
        return list.stream().map(AptDeduction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String genSettlementNo() {
        return "JS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
