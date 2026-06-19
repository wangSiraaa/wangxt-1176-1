package com.apartment.deposit.service;

import com.apartment.deposit.dto.DeductionCreateDTO;
import com.apartment.deposit.entity.AptDeduction;

import java.util.List;

public interface AptDeductionService {

    AptDeduction create(DeductionCreateDTO dto);

    AptDeduction update(Long id, DeductionCreateDTO dto);

    void delete(Long id);

    List<AptDeduction> listByCheckinId(Long checkinId);

    AptDeduction getById(Long id);

    void lockByCheckinId(Long checkinId);
}
