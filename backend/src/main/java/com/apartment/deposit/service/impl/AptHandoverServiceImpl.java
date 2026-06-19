package com.apartment.deposit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.apartment.deposit.dto.HandoverConfirmDTO;
import com.apartment.deposit.entity.AptCheckin;
import com.apartment.deposit.entity.AptHandover;
import com.apartment.deposit.entity.SysUser;
import com.apartment.deposit.enums.CheckinStatusEnum;
import com.apartment.deposit.enums.RoleEnum;
import com.apartment.deposit.exception.BusinessException;
import com.apartment.deposit.mapper.AptCheckinMapper;
import com.apartment.deposit.mapper.AptHandoverMapper;
import com.apartment.deposit.security.SecurityUtil;
import com.apartment.deposit.service.AptHandoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AptHandoverServiceImpl implements AptHandoverService {

    @Autowired
    private AptHandoverMapper aptHandoverMapper;

    @Autowired
    private AptCheckinMapper aptCheckinMapper;

    @Override
    public AptHandover getByCheckinId(Long checkinId, String handoverType) {
        LambdaQueryWrapper<AptHandover> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptHandover::getCheckinId, checkinId)
                .eq(AptHandover::getHandoverType, handoverType);
        return aptHandoverMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptHandover createCheckinHandover(Long checkinId) {
        if (!SecurityUtil.hasRole(RoleEnum.MERCHANT.getCode())) {
            throw new BusinessException("只有招商主管可以发起交接");
        }

        AptCheckin checkin = aptCheckinMapper.selectById(checkinId);
        if (checkin == null) {
            throw new BusinessException("入住单不存在");
        }
        if (!CheckinStatusEnum.PENDING_HANDOVER.getCode().equals(checkin.getStatus())) {
            throw new BusinessException("当前状态不能创建交接");
        }

        AptHandover exist = getByCheckinId(checkinId, "CHECKIN");
        if (exist != null) {
            return exist;
        }

        AptHandover handover = new AptHandover();
        handover.setCheckinId(checkinId);
        handover.setHandoverType("CHECKIN");
        handover.setResidentId(checkin.getResidentId());
        handover.setMerchantId(SecurityUtil.getCurrentUserId());
        handover.setResidentSign(0);
        handover.setMerchantSign(0);
        handover.setConfirmed(0);
        aptHandoverMapper.insert(handover);
        return handover;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptHandover residentSign(Long id) {
        if (!SecurityUtil.hasRole(RoleEnum.RESIDENT.getCode())) {
            throw new BusinessException("只有住户可以签字确认");
        }

        AptHandover handover = aptHandoverMapper.selectById(id);
        if (handover == null) {
            throw new BusinessException("交接记录不存在");
        }
        if (!SecurityUtil.getCurrentUserId().equals(handover.getResidentId())) {
            throw new BusinessException("只能确认自己的交接单");
        }
        handover.setResidentSign(1);
        aptHandoverMapper.updateById(handover);

        checkAndConfirm(handover);
        return aptHandoverMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptHandover merchantSign(Long id, HandoverConfirmDTO dto) {
        if (!SecurityUtil.hasRole(RoleEnum.MERCHANT.getCode())) {
            throw new BusinessException("只有招商主管可以签字确认");
        }

        AptHandover handover = aptHandoverMapper.selectById(id);
        if (handover == null) {
            throw new BusinessException("交接记录不存在");
        }

        if (dto.getMeterWater() != null) handover.setMeterWater(dto.getMeterWater());
        if (dto.getMeterElectric() != null) handover.setMeterElectric(dto.getMeterElectric());
        if (dto.getMeterGas() != null) handover.setMeterGas(dto.getMeterGas());
        if (dto.getKeyCount() != null) handover.setKeyCount(dto.getKeyCount());
        if (dto.getDoorCardCount() != null) handover.setDoorCardCount(dto.getDoorCardCount());
        if (dto.getFurnitureCondition() != null) handover.setFurnitureCondition(dto.getFurnitureCondition());
        if (dto.getApplianceCondition() != null) handover.setApplianceCondition(dto.getApplianceCondition());
        if (dto.getOtherCondition() != null) handover.setOtherCondition(dto.getOtherCondition());
        handover.setMerchantSign(1);
        handover.setHandoverTime(LocalDateTime.now());
        aptHandoverMapper.updateById(handover);

        checkAndConfirm(handover);
        return aptHandoverMapper.selectById(id);
    }

    private void checkAndConfirm(AptHandover handover) {
        AptHandover latest = aptHandoverMapper.selectById(handover.getId());
        if (latest.getResidentSign() == 1 && latest.getMerchantSign() == 1) {
            latest.setConfirmed(1);
            latest.setHandoverTime(LocalDateTime.now());
            aptHandoverMapper.updateById(latest);

            AptCheckin checkin = aptCheckinMapper.selectById(latest.getCheckinId());
            if (checkin != null && CheckinStatusEnum.PENDING_HANDOVER.getCode().equals(checkin.getStatus())) {
                checkin.setHandoverConfirmed(1);
                checkin.setStatus(CheckinStatusEnum.HANDED_OVER.getCode());
                aptCheckinMapper.updateById(checkin);
            }
        }
    }
}
