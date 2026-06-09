package com.company.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.Asset;
import com.company.asset.entity.InventoryItem;
import com.company.asset.entity.InventoryTask;
import com.company.asset.mapper.AssetMapper;
import com.company.asset.mapper.InventoryItemMapper;
import com.company.asset.mapper.InventoryTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryTaskService extends ServiceImpl<InventoryTaskMapper, InventoryTask> {

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private InventoryItemMapper inventoryItemMapper;

    @Transactional
    public InventoryTask createTask(InventoryTask task) {
        String taskNo = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
            + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        task.setTaskNo(taskNo);
        task.setStatus("PENDING");
        task.setTotalCount(0);
        task.setCheckedCount(0);
        task.setSurplusCount(0);
        task.setLossCount(0);
        task.setDifferenceCount(0);
        task.setCreateTime(LocalDateTime.now());
        this.save(task);

        QueryWrapper<Asset> assetQuery = new QueryWrapper<>();
        if ("DEPARTMENT".equals(task.getScopeType()) && task.getScopeValue() != null) {
            assetQuery.eq("department_id", task.getScopeValue());
        } else if ("WAREHOUSE".equals(task.getScopeType()) && task.getScopeValue() != null) {
            assetQuery.like("location", task.getScopeValue());
        }
        List<Asset> assets = assetMapper.selectList(assetQuery);

        for (Asset asset : assets) {
            InventoryItem item = new InventoryItem();
            item.setTaskId(task.getId());
            item.setAssetId(asset.getId());
            item.setAssetNo(asset.getAssetNo());
            item.setAssetName(asset.getName());
            item.setBookStatus(asset.getStatus());
            item.setBookLocation(asset.getLocation());
            item.setBookDepartment(asset.getDepartmentId());
            item.setIsChecked(false);
            item.setCreateTime(LocalDateTime.now());
            inventoryItemMapper.insert(item);
        }

        task.setTotalCount(assets.size());
        this.updateById(task);
        return task;
    }

    @Transactional
    public boolean startTask(Long taskId) {
        InventoryTask task = this.getById(taskId);
        if (task != null && "PENDING".equals(task.getStatus())) {
            task.setStatus("IN_PROGRESS");
            task.setStartTime(LocalDateTime.now());
            return this.updateById(task);
        }
        return false;
    }

    @Transactional
    public int completeTask(Long taskId) {
        InventoryTask task = this.getById(taskId);
        if (task == null || !"IN_PROGRESS".equals(task.getStatus())) {
            return -1;
        }
        QueryWrapper<InventoryItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        queryWrapper.eq("is_checked", 0);
        Long pendingCount = inventoryItemMapper.selectCount(queryWrapper);
        if (pendingCount > 0) {
            return pendingCount.intValue();
        }
        task.setStatus("COMPLETED");
        task.setCompleteTime(LocalDateTime.now());
        this.updateById(task);
        return 0;
    }

    public Page<InventoryTask> pageList(Page<InventoryTask> page, String status, Long assigneeId) {
        QueryWrapper<InventoryTask> queryWrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        if (assigneeId != null) {
            queryWrapper.eq("assignee_id", assigneeId);
        }
        queryWrapper.orderByDesc("create_time");
        return this.page(page, queryWrapper);
    }
}
