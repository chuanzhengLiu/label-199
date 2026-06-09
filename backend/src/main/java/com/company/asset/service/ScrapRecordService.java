package com.company.asset.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.Asset;
import com.company.asset.entity.ScrapRecord;
import com.company.asset.mapper.ScrapRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ScrapRecordService extends ServiceImpl<ScrapRecordMapper, ScrapRecord> {

    @Autowired
    private AssetService assetService;

    @Transactional
    public boolean applyScrap(ScrapRecord record) {
        Asset asset = assetService.getById(record.getAssetId());
        if (asset == null) {
            throw new RuntimeException("Asset not found");
        }
        record.setStatus("PENDING");
        return save(record);
    }

    @Transactional
    public boolean approveScrap(Long id, boolean approved) {
        ScrapRecord record = getById(id);
        if (record == null) {
            throw new RuntimeException("Record not found");
        }

        if (approved) {
            Asset asset = assetService.getById(record.getAssetId());
            if (asset != null) {
                asset.setStatus("SCRAPPED");
                assetService.updateById(asset);
            }
            record.setStatus("APPROVED");
            record.setScrapDate(LocalDate.now());
        } else {
            record.setStatus("REJECTED");
        }
        return updateById(record);
    }
}
