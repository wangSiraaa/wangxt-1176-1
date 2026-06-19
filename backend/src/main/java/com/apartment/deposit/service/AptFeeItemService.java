package com.apartment.deposit.service;

import com.apartment.deposit.entity.AptFeeItem;

import java.util.List;

public interface AptFeeItemService {

    List<AptFeeItem> listByCheckinId(Long checkinId);

    List<AptFeeItem> listUnpaidByCheckinId(Long checkinId);

    AptFeeItem getById(Long id);
}
