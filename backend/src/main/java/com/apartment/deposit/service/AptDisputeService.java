package com.apartment.deposit.service;

import com.apartment.deposit.dto.DisputeCreateDTO;
import com.apartment.deposit.dto.DisputeReviewDTO;
import com.apartment.deposit.entity.AptDispute;

import java.util.List;

public interface AptDisputeService {

    AptDispute create(DisputeCreateDTO dto);

    AptDispute review(Long id, DisputeReviewDTO dto);

    AptDispute getById(Long id);

    List<AptDispute> listByCheckinId(Long checkinId);

    List<AptDispute> listByStatus(String status);
}
