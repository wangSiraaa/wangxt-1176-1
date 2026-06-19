package com.apartment.deposit.controller;

import com.apartment.deposit.common.Result;
import com.apartment.deposit.entity.AptRoom;
import com.apartment.deposit.service.AptRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room")
public class AptRoomController {

    @Autowired
    private AptRoomService aptRoomService;

    @GetMapping("/list")
    public Result<List<AptRoom>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String roomNo) {
        return Result.success(aptRoomService.list(status, building, roomNo));
    }

    @GetMapping("/vacant")
    public Result<List<AptRoom>> listVacant() {
        return Result.success(aptRoomService.listVacant());
    }

    @GetMapping("/{id}")
    public Result<AptRoom> getById(@PathVariable Long id) {
        return Result.success(aptRoomService.getById(id));
    }
}
