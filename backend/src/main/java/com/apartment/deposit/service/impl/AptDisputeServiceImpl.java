package com.apartment.deposit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.apartment.deposit.dto.DisputeCreateDTO;
import com.apartment.deposit.dto.DisputeReviewDTO;
import com.apartment.deposit.entity.AptDeduction;
import com.apartment.deposit.entity.AptDispute;
import com.apartment.deposit.entity.AptSettlement;
import com.apartment.deposit.enums.RoleEnum;
import com.apartment.deposit.exception.BusinessException;
import com.apartment.deposit.mapper.AptDeductionMapper;
import com.apartment.deposit.mapper.AptDisputeMapper;
import com.apartment.deposit.mapper.AptSettlementMapper;
import com.apartment.deposit.security.SecurityUtil;
import com.apartment.deposit.service.AptDisputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AptDisputeServiceImpl implements AptDisputeService {

    @Autowired
    private AptDisputeMapper aptDisputeMapper;

    @Autowired
    private AptDeductionMapper aptDeductionMapper;

    @Autowired
    private AptSettlementMapper aptSettlementMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptDispute create(DisputeCreateDTO dto) {
        if (!SecurityUtil.hasRole(RoleEnum.RESIDENT.getCode())) {
            throw new BusinessException("只有住户可以发起争议复核");
        }

        LambdaQueryWrapper<AptSettlement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptSettlement::getCheckinId, dto.getCheckinId())
                .in(AptSettlement::getStatus, "CONFIRMED", "COMPLETED");
        Long count = aptSettlementMapper.selectCount(wrapper);
        if (count == 0) {
            throw new BusinessException("结算尚未确认，不能发起争议");
        }

        if (dto.getDeductionId() != null) {
            LambdaQueryWrapper<AptDispute> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(AptDispute::getDeductionId, dto.getDeductionId())
                    .in(AptDispute::getStatus, "PENDING", "REVIEWING");
            Long existCount = aptDisputeMapper.selectCount(existWrapper);
            if (existCount > 0) {
                throw new BusinessException("该扣款项已存在待处理的争议申请");
            }
        }

        AptDispute dispute = new AptDispute();
        dispute.setCheckinId(dto.getCheckinId());
        dispute.setDeductionId(dto.getDeductionId());
        dispute.setResidentId(SecurityUtil.getCurrentUserId());
        dispute.setDisputeType(dto.getDisputeType());
        dispute.setContent(dto.getContent());
        dispute.setEvidenceUrls(dto.getEvidenceUrls());
        dispute.setStatus("PENDING");
        aptDisputeMapper.insert(dispute);
        return dispute;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptDispute review(Long id, DisputeReviewDTO dto) {
        if (!SecurityUtil.hasRole(RoleEnum.FINANCE.getCode())
                && !SecurityUtil.hasRole(RoleEnum.MERCHANT.getCode())) {
            throw new BusinessException("只有财务或招商主管可以审核争议");
        }

        AptDispute dispute = aptDisputeMapper.selectById(id);
        if (dispute == null) {
            throw new BusinessException("争议记录不存在");
        }
        if (!"PENDING".equals(dispute.getStatus()) && !"REVIEWING".equals(dispute.getStatus())) {
            throw new BusinessException("当前状态不能审核");
        }

        dispute.setReviewerId(SecurityUtil.getCurrentUserId());
        dispute.setReviewOpinion(dto.getReviewOpinion());
        dispute.setReviewTime(LocalDateTime.now());

        if ("APPROVED".equals(dto.getStatus())) {
            if (dto.getAdjustedAmount() != null && dto.getAdjustedAmount().compareTo(BigDecimal.ZERO) >= 0) {
                dispute.setAdjustedAmount(dto.getAdjustedAmount());
                if (dispute.getDeductionId() != null) {
                    AptDeduction deduction = aptDeductionMapper.selectById(dispute.getDeductionId());
                    if (deduction != null) {
                        deduction.setAmount(dto.getAdjustedAmount());
                        aptDeductionMapper.updateById(deduction);
                    }
                }
            }
            dispute.setStatus("APPROVED");
        } else if ("REJECTED".equals(dto.getStatus())) {
            dispute.setStatus("REJECTED");
        } else {
            dispute.setStatus("REVIEWING");
        }

        aptDisputeMapper.updateById(dispute);
        return dispute;
    }

    @Override
    public AptDispute getById(Long id) {
        AptDispute dispute = aptDisputeMapper.selectById(id);
        if (dispute != null && dispute.getDeductionId() != null) {
            AptDeduction deduction = aptDeductionMapper.selectById(dispute.getDeductionId());
            if (deduction != null) {
                dispute.setDeductionType(deduction.getDeductionType());
                dispute.setDeductionDescription(deduction.getDescription());
                dispute.setDeductionAmount(deduction.getAmount());
            }
        }
        return dispute;
    }

    @Override
    public List<AptDispute> listByCheckinId(Long checkinId) {
        LambdaQueryWrapper<AptDispute> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptDispute::getCheckinId, checkinId)
                .orderByDesc(AptDispute::getCreateTime);
        List<AptDispute> list = aptDisputeMapper.selectList(wrapper);
        for (AptDispute d : list) {
            if (d.getDeductionId() != null) {
                AptDeduction deduction = aptDeductionMapper.selectById(d.getDeductionId());
                if (deduction != null) {
                    d.setDeductionType(deduction.getDeductionType());
                    d.setDeductionDescription(deduction.getDescription());
                    d.setDeductionAmount(deduction.getAmount());
                }
            }
        }
        return list;
    }

    @Override
    public List<AptDispute> listByStatus(String status) {
        LambdaQueryWrapper<AptDispute> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(AptDispute::getStatus, status);
        }
        wrapper.orderByDesc(AptDispute::getCreateTime);
        return aptDisputeMapper.selectList(wrapper);
    }
}
