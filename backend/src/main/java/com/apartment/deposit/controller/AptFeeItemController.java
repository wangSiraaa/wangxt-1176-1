package com.apartment.deposit.controller;

import com.apartment.deposit.common.Result;
import com.apartment.deposit.entity.AptFeeItem;
import com.apartment.deposit.service.AptFeeItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fee")
public class AptFeeItemController {

    @Autowired
    private AptFeeItemService aptFeeItemService;

    @GetMapping("/{id}")
    public Result<AptFeeItem> getById(@PathVariable Long id) {
        return Result.success(aptFeeItemService.getById(id));
    }

    @GetMapping("/listByCheckin/{checkinId}")
    public Result<List<AptFeeItem>> listByCheckinId(@PathVariable Long checkinId) {
        return Result.success(aptFeeItemService.listByCheckinId(checkinId));
    }

    @GetMapping("/listUnpaid/{checkinId}")
    public Result<List<AptFeeItem>> listUnpaidByCheckinId(@PathVariable Long checkinId) {
        return Result.success(aptFeeItemService.listUnpaidByCheckinId(checkinId));
    }
}
