package com.apartment.deposit.controller;

import com.apartment.deposit.common.Result;
import com.apartment.deposit.dto.DeductionCreateDTO;
import com.apartment.deposit.entity.AptDeduction;
import com.apartment.deposit.service.AptDeductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deduction")
public class AptDeductionController {

    @Autowired
    private AptDeductionService aptDeductionService;

    @PostMapping("/create")
    public Result<AptDeduction> create(@Validated @RequestBody DeductionCreateDTO dto) {
        return Result.success(aptDeductionService.create(dto));
    }

    @PutMapping("/update/{id}")
    public Result<AptDeduction> update(@PathVariable Long id, @Validated @RequestBody DeductionCreateDTO dto) {
        return Result.success(aptDeductionService.update(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        aptDeductionService.delete(id);
        return Result.success();
    }

    @GetMapping("/listByCheckin/{checkinId}")
    public Result<List<AptDeduction>> listByCheckinId(@PathVariable Long checkinId) {
        return Result.success(aptDeductionService.listByCheckinId(checkinId));
    }

    @GetMapping("/{id}")
    public Result<AptDeduction> getById(@PathVariable Long id) {
        return Result.success(aptDeductionService.getById(id));
    }
}
