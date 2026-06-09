package com.company.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.InventoryItem;
import com.company.asset.entity.InventoryTask;
import com.company.asset.mapper.InventoryItemMapper;
import com.company.asset.mapper.InventoryTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryItemService extends ServiceImpl<InventoryItemMapper, InventoryItem> {

    @Autowired
    private InventoryTaskMapper inventoryTaskMapper;

    public Page<InventoryItem> getTaskItemsPage(Page<InventoryItem> page, Long taskId, String resultType) {
        QueryWrapper<InventoryItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        if (resultType != null && !resultType.isEmpty() && !"ALL".equals(resultType)) {
            if ("PENDING".equals(resultType)) {
                queryWrapper.eq("is_checked", 0);
            } else {
                queryWrapper.eq("result_type", resultType);
            }
        }
        queryWrapper.orderByAsc("id");
        return this.page(page, queryWrapper);
    }

    @Transactional
    public InventoryItem checkItem(InventoryItem item) {
        InventoryItem existing = this.getById(item.getId());
        if (existing == null) {
            return null;
        }

        existing.setActualStatus(item.getActualStatus());
        existing.setActualLocation(item.getActualLocation());
        existing.setRemarks(item.getRemarks());
        existing.setIsChecked(true);
        existing.setCheckTime(LocalDateTime.now());

        String resultType;
        if (existing.getAssetId() == null) {
            resultType = "SURPLUS";
        } else if ("LOST".equals(item.getActualStatus())) {
            resultType = "LOSS";
        } else {
            boolean statusDiff = existing.getBookStatus() != null 
                && !existing.getBookStatus().equals(item.getActualStatus());
            String locToCompare = (item.getActualLocation() == null || item.getActualLocation().isEmpty()) 
                ? existing.getBookLocation() : item.getActualLocation();
            boolean locationDiff = existing.getBookLocation() != null 
                && !existing.getBookLocation().equals(locToCompare);
            
            if (statusDiff && locationDiff) {
                resultType = "BOTH_DIFF";
            } else if (statusDiff) {
                resultType = "STATUS_DIFF";
            } else if (locationDiff) {
                resultType = "LOCATION_DIFF";
            } else {
                resultType = "MATCH";
            }
        }
        existing.setResultType(resultType);
        this.updateById(existing);

        this.recalculateTaskStats(existing.getTaskId());
        
        return existing;
    }

    @Transactional
    public InventoryItem addSurplusItem(Long taskId, InventoryItem item) {
        item.setTaskId(taskId);
        item.setIsChecked(true);
        item.setResultType("SURPLUS");
        item.setCheckTime(LocalDateTime.now());
        item.setCreateTime(LocalDateTime.now());
        this.save(item);
        
        this.recalculateTaskStats(taskId);
        
        return item;
    }

    private void recalculateTaskStats(Long taskId) {
        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) return;

        QueryWrapper<InventoryItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        List<InventoryItem> allItems = this.list(queryWrapper);

        int checked = 0, surplus = 0, loss = 0, diff = 0;
        for (InventoryItem item : allItems) {
            if (Boolean.TRUE.equals(item.getIsChecked())) {
                checked++;
                String rt = item.getResultType();
                if ("SURPLUS".equals(rt)) surplus++;
                if ("LOSS".equals(rt)) loss++;
                if (rt != null && !"MATCH".equals(rt)) diff++;
            }
        }

        task.setTotalCount(allItems.size());
        task.setCheckedCount(checked);
        task.setSurplusCount(surplus);
        task.setLossCount(loss);
        task.setDifferenceCount(diff);
        inventoryTaskMapper.updateById(task);
    }

    public Map<String, Object> getReport(Long taskId) {
        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) return null;

        Map<String, Object> report = new HashMap<>();
        report.put("task", task);

        QueryWrapper<InventoryItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        List<InventoryItem> allItems = this.list(queryWrapper);
        report.put("items", allItems);

        int matchCount = 0, surplusCount = 0, lossCount = 0, statusDiff = 0, locationDiff = 0, bothDiff = 0, pendingCount = 0;
        for (InventoryItem item : allItems) {
            if (!Boolean.TRUE.equals(item.getIsChecked())) {
                pendingCount++;
                continue;
            }
            String rt = item.getResultType();
            if (rt == null) { pendingCount++; continue; }
            switch (rt) {
                case "MATCH": matchCount++; break;
                case "SURPLUS": surplusCount++; break;
                case "LOSS": lossCount++; break;
                case "STATUS_DIFF": statusDiff++; break;
                case "LOCATION_DIFF": locationDiff++; break;
                case "BOTH_DIFF": bothDiff++; break;
            }
        }

        Map<String, Integer> summary = new HashMap<>();
        summary.put("matchCount", matchCount);
        summary.put("surplusCount", surplusCount);
        summary.put("lossCount", lossCount);
        summary.put("statusDiff", statusDiff);
        summary.put("locationDiff", locationDiff);
        summary.put("bothDiff", bothDiff);
        summary.put("pendingCount", pendingCount);
        report.put("summary", summary);

        return report;
    }
}
