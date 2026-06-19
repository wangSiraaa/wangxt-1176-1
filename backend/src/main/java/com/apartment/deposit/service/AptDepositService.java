package com.apartment.deposit.service;

import com.apartment.deposit.dto.DepositCreateDTO;
import com.apartment.deposit.entity.AptDeposit;

import java.util.List;

public interface AptDepositService {

    AptDeposit collectDeposit(DepositCreateDTO dto);

    AptDeposit refundDeposit(DepositCreateDTO dto);

    List<AptDeposit> listByCheckinId(Long checkinId);

    AptDeposit getById(Long id);
}
