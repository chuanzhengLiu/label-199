package com.company.asset.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.Asset;
import com.company.asset.entity.MaintenanceRecord;
import com.company.asset.mapper.MaintenanceRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceRecordService extends ServiceImpl<MaintenanceRecordMapper, MaintenanceRecord> {

    @Autowired
    private AssetService assetService;

    @Transactional
    public boolean reportMaintenance(MaintenanceRecord record) {
        Asset asset = assetService.getById(record.getAssetId());
        if (asset == null) {
            throw new RuntimeException("Asset not found");
        }

        asset.setStatus("MAINTENANCE");
        assetService.updateById(asset);

        record.setStatus("PENDING");
        return save(record);
    }

    @Transactional
    public boolean completeMaintenance(MaintenanceRecord record) {
        MaintenanceRecord existing = getById(record.getId());
        if (existing == null) {
            throw new RuntimeException("Record not found");
        }

        Asset asset = assetService.getById(existing.getAssetId());
        if (asset != null) {
            asset.setStatus("NORMAL");
            assetService.updateById(asset);
        }

        existing.setCost(record.getCost());
        existing.setVendor(record.getVendor());
        existing.setEndDate(record.getEndDate());
        existing.setStatus("COMPLETED");
        return updateById(existing);
    }
}
