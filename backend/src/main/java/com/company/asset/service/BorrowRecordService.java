package com.company.asset.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.Asset;
import com.company.asset.entity.BorrowRecord;
import com.company.asset.mapper.BorrowRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BorrowRecordService extends ServiceImpl<BorrowRecordMapper, BorrowRecord> {

    @Autowired
    private AssetService assetService;

    @Transactional
    public boolean borrowAsset(BorrowRecord borrowRecord) {
        Asset asset = assetService.getById(borrowRecord.getAssetId());
        if (asset == null || !"NORMAL".equals(asset.getStatus())) {
            throw new RuntimeException("Asset not available for borrowing");
        }

        asset.setStatus("BORROWED");
        asset.setCurrentUserId(borrowRecord.getUserId());
        assetService.updateById(asset);

        borrowRecord.setBorrowDate(LocalDateTime.now());
        borrowRecord.setStatus("BORROWED");
        return save(borrowRecord);
    }

    @Transactional
    public boolean returnAsset(Long id) {
        BorrowRecord record = getById(id);
        if (record == null || "RETURNED".equals(record.getStatus())) {
            throw new RuntimeException("Invalid return record");
        }

        Asset asset = assetService.getById(record.getAssetId());
        if (asset != null) {
            asset.setStatus("NORMAL");
            asset.setCurrentUserId(null);
            assetService.updateById(asset);
        }

        record.setActualReturnDate(LocalDateTime.now());
        record.setStatus("RETURNED");
        return updateById(record);
    }
}
