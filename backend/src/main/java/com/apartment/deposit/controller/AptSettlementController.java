package com.apartment.deposit.controller;

import com.apartment.deposit.common.Result;
import com.apartment.deposit.dto.SettlementCreateDTO;
import com.apartment.deposit.entity.AptSettlement;
import com.apartment.deposit.service.AptSettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/settlement")
public class AptSettlementController {

    @Autowired
    private AptSettlementService aptSettlementService;

    @PostMapping("/create")
    public Result<AptSettlement> create(@Validated @RequestBody SettlementCreateDTO dto) {
        return Result.success(aptSettlementService.create(dto));
    }

    @PostMapping("/confirm/{id}")
    public Result<AptSettlement> confirm(@PathVariable Long id) {
        return Result.success(aptSettlementService.confirm(id));
    }

    @PostMapping("/complete/{id}")
    public Result<AptSettlement> complete(@PathVariable Long id) {
        return Result.success(aptSettlementService.complete(id));
    }

    @GetMapping("/{id}")
    public Result<AptSettlement> getById(@PathVariable Long id) {
        return Result.success(aptSettlementService.getById(id));
    }

    @GetMapping("/getByCheckin/{checkinId}")
    public Result<AptSettlement> getByCheckinId(@PathVariable Long checkinId) {
        return Result.success(aptSettlementService.getByCheckinId(checkinId));
    }

    @GetMapping("/list")
    public Result<List<AptSettlement>> list(@RequestParam(required = false) String status) {
        return Result.success(aptSettlementService.list(status));
    }
}
