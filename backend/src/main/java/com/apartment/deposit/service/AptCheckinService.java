package com.apartment.deposit.service;

import com.apartment.deposit.dto.CheckinCreateDTO;
import com.apartment.deposit.entity.AptCheckin;

import java.util.List;

public interface AptCheckinService {

    AptCheckin create(CheckinCreateDTO dto);

    AptCheckin getById(Long id);

    List<AptCheckin> list(String status, Long residentId);

    AptCheckin submitHandover(Long id);

    AptCheckin confirmCheckin(Long id);

    AptCheckin startSettlement(Long id);

    AptCheckin cancel(Long id);
}
