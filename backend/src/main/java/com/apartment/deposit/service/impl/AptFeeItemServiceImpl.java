package com.apartment.deposit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.apartment.deposit.entity.AptFeeItem;
import com.apartment.deposit.mapper.AptFeeItemMapper;
import com.apartment.deposit.service.AptFeeItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AptFeeItemServiceImpl implements AptFeeItemService {

    @Autowired
    private AptFeeItemMapper aptFeeItemMapper;

    @Override
    public List<AptFeeItem> listByCheckinId(Long checkinId) {
        LambdaQueryWrapper<AptFeeItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptFeeItem::getCheckinId, checkinId)
                .orderByDesc(AptFeeItem::getCreateTime);
        return aptFeeItemMapper.selectList(wrapper);
    }

    @Override
    public List<AptFeeItem> listUnpaidByCheckinId(Long checkinId) {
        LambdaQueryWrapper<AptFeeItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AptFeeItem::getCheckinId, checkinId)
                .eq(AptFeeItem::getPaid, 0)
                .orderByDesc(AptFeeItem::getCreateTime);
        return aptFeeItemMapper.selectList(wrapper);
    }

    @Override
    public AptFeeItem getById(Long id) {
        return aptFeeItemMapper.selectById(id);
    }
}
