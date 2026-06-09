package com.company.asset.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.asset.common.Result;
import com.company.asset.entity.MaintenanceRecord;
import com.company.asset.entity.Asset;
import com.company.asset.service.AssetService;
import com.company.asset.service.MaintenanceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceRecordController {

    @Autowired
    private MaintenanceRecordService maintenanceRecordService;

    @Autowired
    private AssetService assetService;

    @GetMapping("/list")
    public Result<Page<MaintenanceRecord>> list(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        Page<MaintenanceRecord> pageParam = new Page<>(page, size);
        QueryWrapper<MaintenanceRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        
        Page<MaintenanceRecord> resultPage = maintenanceRecordService.page(pageParam, queryWrapper);
        List<MaintenanceRecord> records = resultPage.getRecords();
        
        if (!records.isEmpty()) {
            Set<Long> assetIds = records.stream().map(MaintenanceRecord::getAssetId).collect(Collectors.toSet());
            List<Asset> assets = assetService.listByIds(assetIds);
            Map<Long, String> assetMap = assets.stream().collect(Collectors.toMap(Asset::getId, Asset::getName));
            
            records.forEach(record -> record.setAssetName(assetMap.get(record.getAssetId())));
        }
        
        return Result.success(resultPage);
    }

    @PostMapping("/report")
    public Result<Boolean> report(@RequestBody MaintenanceRecord record) {
        try {
            return Result.success(maintenanceRecordService.reportMaintenance(record));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/complete")
    public Result<Boolean> complete(@RequestBody MaintenanceRecord record) {
        try {
            return Result.success(maintenanceRecordService.completeMaintenance(record));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
