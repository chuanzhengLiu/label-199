package com.company.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.Asset;
import com.company.asset.entity.InventoryDetail;
import com.company.asset.entity.InventoryTask;
import com.company.asset.mapper.InventoryDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryDetailService extends ServiceImpl<InventoryDetailMapper, InventoryDetail> {

    @Autowired
    private InventoryTaskService inventoryTaskService;

    @Autowired
    private AssetService assetService;

    public List<InventoryDetail> getDetailList(Long taskId, String checkResult, Integer isChecked) {
        QueryWrapper<InventoryDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        if (checkResult != null && !checkResult.isEmpty()) {
            queryWrapper.eq("check_result", checkResult);
        }
        if (isChecked != null) {
            queryWrapper.eq("is_checked", isChecked);
        }
        queryWrapper.orderByAsc("id");
        return list(queryWrapper);
    }

    @Transactional
    public boolean checkDetail(InventoryDetail detail) {
        InventoryTask task = inventoryTaskService.getById(detail.getTaskId());
        if (task == null) {
            return false;
        }
        
        InventoryDetail existDetail = getById(detail.getId());
        if (existDetail == null) {
            return false;
        }
        
        boolean wasChecked = existDetail.getIsChecked() != null && existDetail.getIsChecked() == 1;
        
        detail.setIsChecked(1);
        detail.setCheckTime(LocalDateTime.now());
        
        String checkResult = "NORMAL";
        if (detail.getAssetId() == null) {
            checkResult = "PROFIT";
        } else {
            boolean statusDiff = (detail.getActualStatus() != null && !detail.getActualStatus().equals(existDetail.getBookStatus()));
            boolean locationDiff = (detail.getActualLocation() != null && !detail.getActualLocation().equals(existDetail.getBookLocation()));
            if (statusDiff && locationDiff) {
                checkResult = "BOTH_DIFF";
            } else if (statusDiff) {
                checkResult = "STATUS_DIFF";
            } else if (locationDiff) {
                checkResult = "LOCATION_DIFF";
            }
        }
        detail.setCheckResult(checkResult);
        
        updateById(detail);
        
        if (!wasChecked) {
            task.setCheckedCount(task.getCheckedCount() + 1);
            if ("PROFIT".equals(checkResult)) {
                task.setProfitCount(task.getProfitCount() + 1);
            }
            inventoryTaskService.updateById(task);
        }
        
        return true;
    }

    @Transactional
    public boolean markLoss(Long taskId, Long detailId) {
        InventoryTask task = inventoryTaskService.getById(taskId);
        if (task == null) {
            return false;
        }
        
        InventoryDetail detail = getById(detailId);
        if (detail == null) {
            return false;
        }
        
        if (detail.getIsChecked() == null || detail.getIsChecked() == 0) {
            detail.setIsChecked(1);
            detail.setCheckTime(LocalDateTime.now());
            task.setCheckedCount(task.getCheckedCount() + 1);
        }
        
        detail.setCheckResult("LOSS");
        updateById(detail);
        
        task.setLossCount(task.getLossCount() + 1);
        inventoryTaskService.updateById(task);
        
        return true;
    }

    @Transactional
    public boolean addProfit(InventoryDetail detail) {
        InventoryTask task = inventoryTaskService.getById(detail.getTaskId());
        if (task == null) {
            return false;
        }
        
        detail.setIsChecked(1);
        detail.setCheckTime(LocalDateTime.now());
        detail.setCheckResult("PROFIT");
        save(detail);
        
        task.setTotalCount(task.getTotalCount() + 1);
        task.setCheckedCount(task.getCheckedCount() + 1);
        task.setProfitCount(task.getProfitCount() + 1);
        inventoryTaskService.updateById(task);
        
        return true;
    }

    public InventoryDetail scanAsset(Long taskId, String assetNo) {
        QueryWrapper<InventoryDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        queryWrapper.eq("asset_no", assetNo);
        InventoryDetail detail = getOne(queryWrapper);
        if (detail == null) {
            QueryWrapper<Asset> assetWrapper = new QueryWrapper<>();
            assetWrapper.eq("asset_no", assetNo);
            Asset asset = assetService.getOne(assetWrapper);
            if (asset != null) {
                detail = new InventoryDetail();
                detail.setTaskId(taskId);
                detail.setAssetId(asset.getId());
                detail.setAssetNo(asset.getAssetNo());
                detail.setAssetName(asset.getName());
                detail.setBookStatus(asset.getStatus());
                detail.setBookLocation(asset.getLocation());
                detail.setBookDepartmentId(asset.getDepartmentId());
                detail.setIsChecked(0);
                detail.setCheckResult("PROFIT");
            }
        }
        return detail;
    }
}
