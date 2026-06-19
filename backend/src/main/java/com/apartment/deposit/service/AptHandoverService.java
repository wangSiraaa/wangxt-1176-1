package com.apartment.deposit.service;

import com.apartment.deposit.dto.HandoverConfirmDTO;
import com.apartment.deposit.entity.AptHandover;

public interface AptHandoverService {

    AptHandover getByCheckinId(Long checkinId, String handoverType);

    AptHandover createCheckinHandover(Long checkinId);

    AptHandover residentSign(Long id);

    AptHandover merchantSign(Long id, HandoverConfirmDTO dto);
}
