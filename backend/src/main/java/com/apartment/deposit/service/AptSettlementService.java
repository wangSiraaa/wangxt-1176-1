package com.apartment.deposit.service;

import com.apartment.deposit.dto.SettlementCreateDTO;
import com.apartment.deposit.entity.AptSettlement;

import java.util.List;

public interface AptSettlementService {

    AptSettlement create(SettlementCreateDTO dto);

    AptSettlement confirm(Long id);

    AptSettlement complete(Long id);

    AptSettlement getById(Long id);

    AptSettlement getByCheckinId(Long checkinId);

    List<AptSettlement> list(String status);
}
