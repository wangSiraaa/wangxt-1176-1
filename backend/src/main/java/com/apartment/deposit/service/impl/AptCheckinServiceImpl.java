package com.apartment.deposit.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.apartment.deposit.dto.CheckinCreateDTO;
import com.apartment.deposit.entity.AptCheckin;
import com.apartment.deposit.entity.AptRoom;
import com.apartment.deposit.entity.SysUser;
import com.apartment.deposit.enums.CheckinStatusEnum;
import com.apartment.deposit.enums.RoleEnum;
import com.apartment.deposit.enums.RoomStatusEnum;
import com.apartment.deposit.exception.BusinessException;
import com.apartment.deposit.mapper.AptCheckinMapper;
import com.apartment.deposit.mapper.AptRoomMapper;
import com.apartment.deposit.security.SecurityUtil;
import com.apartment.deposit.service.AptCheckinService;
import com.apartment.deposit.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AptCheckinServiceImpl implements AptCheckinService {

    @Autowired
    private AptCheckinMapper aptCheckinMapper;

    @Autowired
    private AptRoomMapper aptRoomMapper;

    @Autowired
    private SysUserService sysUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptCheckin create(CheckinCreateDTO dto) {
        if (!SecurityUtil.hasRole(RoleEnum.MERCHANT.getCode())) {
            throw new BusinessException("只有招商主管可以创建入住单");
        }

        AptRoom room = aptRoomMapper.selectById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException("房间不存在");
        }
        if (!RoomStatusEnum.VACANT.getCode().equals(room.getStatus())) {
            throw new BusinessException("该房间当前不可入住");
        }

        SysUser resident = sysUserService.getById(dto.getResidentId());
        if (resident == null || !RoleEnum.RESIDENT.getCode().equals(resident.getRoleCode())) {
            throw new BusinessException("住户信息无效");
        }

        AptCheckin checkin = new AptCheckin();
        checkin.setCheckinNo(genCheckinNo());
        checkin.setRoomId(dto.getRoomId());
        checkin.setResidentId(dto.getResidentId());
        checkin.setMerchantId(SecurityUtil.getCurrentUserId());
        checkin.setCheckinDate(dto.getCheckinDate());
        checkin.setCheckoutDate(dto.getCheckoutDate());
        checkin.setMonthlyRent(dto.getMonthlyRent());
        checkin.setDepositAmount(dto.getDepositAmount());
        checkin.setStatus(CheckinStatusEnum.DRAFT.getCode());
        checkin.setHandoverConfirmed(0);
        checkin.setRemark(dto.getRemark());

        aptCheckinMapper.insert(checkin);
        return checkin;
    }

    private String genCheckinNo() {
        return "RZ" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    @Override
    public AptCheckin getById(Long id) {
        return aptCheckinMapper.selectById(id);
    }

    @Override
    public List<AptCheckin> list(String status, Long residentId) {
        LambdaQueryWrapper<AptCheckin> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(AptCheckin::getStatus, status);
        }
        if (residentId != null) {
            wrapper.eq(AptCheckin::getResidentId, residentId);
        }
        if (SecurityUtil.hasRole(RoleEnum.RESIDENT.getCode())) {
            wrapper.eq(AptCheckin::getResidentId, SecurityUtil.getCurrentUserId());
        }
        wrapper.orderByDesc(AptCheckin::getCreateTime);
        return aptCheckinMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptCheckin submitHandover(Long id) {
        AptCheckin checkin = getAndValidate(id);
        if (!CheckinStatusEnum.DRAFT.getCode().equals(checkin.getStatus())) {
            throw new BusinessException("当前状态不能提交交接");
        }
        checkin.setStatus(CheckinStatusEnum.PENDING_HANDOVER.getCode());
        aptCheckinMapper.updateById(checkin);
        return checkin;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptCheckin confirmCheckin(Long id) {
        AptCheckin checkin = getAndValidate(id);
        if (!CheckinStatusEnum.HANDED_OVER.getCode().equals(checkin.getStatus())) {
            throw new BusinessException("当前状态不能确认入住");
        }
        if (checkin.getHandoverConfirmed() == null || checkin.getHandoverConfirmed() == 0) {
            throw new BusinessException("房间交接未确认，不能入住");
        }
        checkin.setStatus(CheckinStatusEnum.CHECKED_IN.getCode());
        checkin.setActualCheckinDate(LocalDate.now());

        AptRoom room = aptRoomMapper.selectById(checkin.getRoomId());
        if (room != null) {
            room.setStatus(RoomStatusEnum.OCCUPIED.getCode());
            aptRoomMapper.updateById(room);
        }

        aptCheckinMapper.updateById(checkin);
        return checkin;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptCheckin startSettlement(Long id) {
        AptCheckin checkin = getAndValidate(id);
        if (!CheckinStatusEnum.CHECKED_IN.getCode().equals(checkin.getStatus())) {
            throw new BusinessException("当前状态不能发起结算");
        }
        checkin.setStatus(CheckinStatusEnum.SETTLING.getCode());
        aptCheckinMapper.updateById(checkin);
        return checkin;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AptCheckin cancel(Long id) {
        AptCheckin checkin = getAndValidate(id);
        if (CheckinStatusEnum.SETTLED.getCode().equals(checkin.getStatus())
                || CheckinStatusEnum.CHECKED_IN.getCode().equals(checkin.getStatus())) {
            throw new BusinessException("当前状态不能取消");
        }
        checkin.setStatus(CheckinStatusEnum.CANCELLED.getCode());
        aptCheckinMapper.updateById(checkin);
        return checkin;
    }

    private AptCheckin getAndValidate(Long id) {
        AptCheckin checkin = aptCheckinMapper.selectById(id);
        if (checkin == null) {
            throw new BusinessException("入住单不存在");
        }
        return checkin;
    }
}
