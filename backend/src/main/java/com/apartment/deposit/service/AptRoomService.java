package com.apartment.deposit.service;

import com.apartment.deposit.entity.AptRoom;

import java.util.List;

public interface AptRoomService {

    List<AptRoom> list(String status, String building, String roomNo);

    AptRoom getById(Long id);

    List<AptRoom> listVacant();
}
