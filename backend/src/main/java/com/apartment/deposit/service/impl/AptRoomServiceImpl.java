package com.apartment.deposit.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.apartment.deposit.entity.AptRoom;
import com.apartment.deposit.enums.RoomStatusEnum;
import com.apartment.deposit.mapper.AptRoomMapper;
import com.apartment.deposit.service.AptRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AptRoomServiceImpl implements AptRoomService {

    @Autowired
    private AptRoomMapper aptRoomMapper;

    @Override
    public List<AptRoom> list(String status, String building, String roomNo) {
        LambdaQueryWrapper<AptRoom> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(AptRoom::getStatus, status);
        }
        if (StrUtil.isNotBlank(building)) {
            wrapper.like(AptRoom::getBuilding, building);
        }
        if (StrUtil.isNotBlank(roomNo)) {
            wrapper.like(AptRoom::getRoomNo, roomNo);
        }
        wrapper.orderByAsc(AptRoom::getBuilding, AptRoom::getFloor, AptRoom::getRoomNo);
        return aptRoomMapper.selectList(wrapper);
    }

    @Override
    public AptRoom getById(Long id) {
        return aptRoomMapper.selectById(id);
    }

    @Override
    public List<AptRoom> listVacant() {
        return list(RoomStatusEnum.VACANT.getCode(), null, null);
    }
}
