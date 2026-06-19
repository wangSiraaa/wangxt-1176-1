package com.apartment.deposit.controller;

import com.apartment.deposit.common.Result;
import com.apartment.deposit.dto.DepositCreateDTO;
import com.apartment.deposit.entity.AptDeposit;
import com.apartment.deposit.service.AptDepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deposit")
public class AptDepositController {

    @Autowired
    private AptDepositService aptDepositService;

    @PostMapping("/collect")
    public Result<AptDeposit> collectDeposit(@Validated @RequestBody DepositCreateDTO dto) {
        return Result.success(aptDepositService.collectDeposit(dto));
    }

    @PostMapping("/refund")
    public Result<AptDeposit> refundDeposit(@Validated @RequestBody DepositCreateDTO dto) {
        return Result.success(aptDepositService.refundDeposit(dto));
    }

    @GetMapping("/listByCheckin/{checkinId}")
    public Result<List<AptDeposit>> listByCheckinId(@PathVariable Long checkinId) {
        return Result.success(aptDepositService.listByCheckinId(checkinId));
    }

    @GetMapping("/{id}")
    public Result<AptDeposit> getById(@PathVariable Long id) {
        return Result.success(aptDepositService.getById(id));
    }
}
