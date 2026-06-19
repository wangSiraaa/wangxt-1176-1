package com.apartment.deposit.controller;

import com.apartment.deposit.common.Result;
import com.apartment.deposit.dto.DisputeCreateDTO;
import com.apartment.deposit.dto.DisputeReviewDTO;
import com.apartment.deposit.entity.AptDispute;
import com.apartment.deposit.service.AptDisputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dispute")
public class AptDisputeController {

    @Autowired
    private AptDisputeService aptDisputeService;

    @PostMapping("/create")
    public Result<AptDispute> create(@Validated @RequestBody DisputeCreateDTO dto) {
        return Result.success(aptDisputeService.create(dto));
    }

    @PostMapping("/review/{id}")
    public Result<AptDispute> review(@PathVariable Long id, @RequestBody DisputeReviewDTO dto) {
        return Result.success(aptDisputeService.review(id, dto));
    }

    @GetMapping("/{id}")
    public Result<AptDispute> getById(@PathVariable Long id) {
        return Result.success(aptDisputeService.getById(id));
    }

    @GetMapping("/listByCheckin/{checkinId}")
    public Result<List<AptDispute>> listByCheckinId(@PathVariable Long checkinId) {
        return Result.success(aptDisputeService.listByCheckinId(checkinId));
    }

    @GetMapping("/list")
    public Result<List<AptDispute>> list(@RequestParam(required = false) String status) {
        return Result.success(aptDisputeService.listByStatus(status));
    }
}
