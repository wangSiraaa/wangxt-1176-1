package com.apartment.deposit.controller;

import com.apartment.deposit.common.Result;
import com.apartment.deposit.dto.CheckinCreateDTO;
import com.apartment.deposit.entity.AptCheckin;
import com.apartment.deposit.service.AptCheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/checkin")
public class AptCheckinController {

    @Autowired
    private AptCheckinService aptCheckinService;

    @PostMapping("/create")
    public Result<AptCheckin> create(@Validated @RequestBody CheckinCreateDTO dto) {
        return Result.success(aptCheckinService.create(dto));
    }

    @GetMapping("/{id}")
    public Result<AptCheckin> getById(@PathVariable Long id) {
        return Result.success(aptCheckinService.getById(id));
    }

    @GetMapping("/list")
    public Result<List<AptCheckin>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long residentId) {
        return Result.success(aptCheckinService.list(status, residentId));
    }

    @PostMapping("/submitHandover/{id}")
    public Result<AptCheckin> submitHandover(@PathVariable Long id) {
        return Result.success(aptCheckinService.submitHandover(id));
    }

    @PostMapping("/confirmCheckin/{id}")
    public Result<AptCheckin> confirmCheckin(@PathVariable Long id) {
        return Result.success(aptCheckinService.confirmCheckin(id));
    }

    @PostMapping("/startSettlement/{id}")
    public Result<AptCheckin> startSettlement(@PathVariable Long id) {
        return Result.success(aptCheckinService.startSettlement(id));
    }

    @PostMapping("/cancel/{id}")
    public Result<AptCheckin> cancel(@PathVariable Long id) {
        return Result.success(aptCheckinService.cancel(id));
    }
}
