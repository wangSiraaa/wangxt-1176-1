package com.apartment.deposit.controller;

import com.apartment.deposit.common.Result;
import com.apartment.deposit.dto.HandoverConfirmDTO;
import com.apartment.deposit.entity.AptHandover;
import com.apartment.deposit.service.AptHandoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/handover")
public class AptHandoverController {

    @Autowired
    private AptHandoverService aptHandoverService;

    @GetMapping("/getByCheckin")
    public Result<AptHandover> getByCheckinId(
            @RequestParam Long checkinId,
            @RequestParam(defaultValue = "CHECKIN") String handoverType) {
        return Result.success(aptHandoverService.getByCheckinId(checkinId, handoverType));
    }

    @PostMapping("/createCheckin/{checkinId}")
    public Result<AptHandover> createCheckinHandover(@PathVariable Long checkinId) {
        return Result.success(aptHandoverService.createCheckinHandover(checkinId));
    }

    @PostMapping("/residentSign/{id}")
    public Result<AptHandover> residentSign(@PathVariable Long id) {
        return Result.success(aptHandoverService.residentSign(id));
    }

    @PostMapping("/merchantSign/{id}")
    public Result<AptHandover> merchantSign(@PathVariable Long id, @RequestBody HandoverConfirmDTO dto) {
        return Result.success(aptHandoverService.merchantSign(id, dto));
    }
}
