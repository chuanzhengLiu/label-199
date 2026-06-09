package com.company.asset.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.asset.common.Result;
import com.company.asset.entity.BorrowRecord;
import com.company.asset.entity.Asset;
import com.company.asset.service.AssetService;
import com.company.asset.service.BorrowRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/borrow")
public class BorrowRecordController {

    @Autowired
    private BorrowRecordService borrowRecordService;

    @Autowired
    private AssetService assetService;

    @GetMapping("/list")
    public Result<Page<BorrowRecord>> list(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) Long userId) {
        Page<BorrowRecord> pageParam = new Page<>(page, size);
        QueryWrapper<BorrowRecord> queryWrapper = new QueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        queryWrapper.orderByDesc("create_time");
        
        Page<BorrowRecord> resultPage = borrowRecordService.page(pageParam, queryWrapper);
        List<BorrowRecord> records = resultPage.getRecords();
        
        if (!records.isEmpty()) {
            Set<Long> assetIds = records.stream().map(BorrowRecord::getAssetId).collect(Collectors.toSet());
            List<Asset> assets = assetService.listByIds(assetIds);
            Map<Long, String> assetMap = assets.stream().collect(Collectors.toMap(Asset::getId, Asset::getName));
            
            records.forEach(record -> record.setAssetName(assetMap.get(record.getAssetId())));
        }
        
        return Result.success(resultPage);
    }

    @PostMapping("/apply")
    public Result<Boolean> borrow(@RequestBody BorrowRecord record) {
        try {
            return Result.success(borrowRecordService.borrowAsset(record));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/return/{id}")
    public Result<Boolean> returnAsset(@PathVariable Long id) {
        try {
            return Result.success(borrowRecordService.returnAsset(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
