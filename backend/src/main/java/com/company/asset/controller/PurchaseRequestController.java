package com.company.asset.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.asset.common.Result;
import com.company.asset.entity.PurchaseRequest;
import com.company.asset.service.PurchaseRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseRequestController {

    @Autowired
    private PurchaseRequestService purchaseRequestService;

    @GetMapping("/list")
    public Result<Page<PurchaseRequest>> list(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) Long applicantId) {
        Page<PurchaseRequest> pageParam = new Page<>(page, size);
        QueryWrapper<PurchaseRequest> queryWrapper = new QueryWrapper<>();
        if (applicantId != null) {
            queryWrapper.eq("applicant_id", applicantId);
        }
        queryWrapper.orderByDesc("create_time");
        return Result.success(purchaseRequestService.page(pageParam, queryWrapper));
    }

    @PostMapping("/apply")
    public Result<Boolean> apply(@RequestBody PurchaseRequest request) {
        return Result.success(purchaseRequestService.apply(request));
    }
}
