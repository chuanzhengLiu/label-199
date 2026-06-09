package com.company.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.Asset;
import com.company.asset.entity.InventoryDetail;
import com.company.asset.entity.InventoryTask;
import com.company.asset.mapper.InventoryTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InventoryTaskService extends ServiceImpl<InventoryTaskMapper, InventoryTask> {

    @Autowired
    private AssetService assetService;

    @Autowired
    private InventoryDetailService inventoryDetailService;

    @Transactional
    public InventoryTask createTask(InventoryTask task) {
        task.setTaskNo(generateTaskNo());
        task.setStatus("PENDING");
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        save(task);

        QueryWrapper<Asset> queryWrapper = new QueryWrapper<>();
        if (task.getDepartmentId() != null && !task.getDepartmentId().isEmpty()) {
            queryWrapper.eq("department_id", task.getDepartmentId());
        }
        if (task.getLocation() != null && !task.getLocation().isEmpty()) {
            queryWrapper.eq("location", task.getLocation());
        }
        List<Asset> assets = assetService.list(queryWrapper);

        for (Asset asset : assets) {
            InventoryDetail detail = new InventoryDetail();
            detail.setTaskId(task.getId());
            detail.setAssetId(asset.getId());
            detail.setAssetNo(asset.getAssetNo());
            detail.setAssetName(asset.getName());
            detail.setBookStatus(asset.getStatus());
            detail.setBookLocation(asset.getLocation());
            detail.setBookDepartment(asset.getDepartmentId());
            detail.setCreateTime(LocalDateTime.now());
            detail.setUpdateTime(LocalDateTime.now());
            inventoryDetailService.save(detail);
        }

        return task;
    }

    @Transactional
    public boolean startTask(Long taskId) {
        InventoryTask task = getById(taskId);
        if (task == null || !"PENDING".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不允许开始");
        }
        task.setStatus("IN_PROGRESS");
        task.setStartTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        return updateById(task);
    }

    @Transactional
    public boolean completeTask(Long taskId) {
        InventoryTask task = getById(taskId);
        if (task == null || !"IN_PROGRESS".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不允许完成");
        }

        QueryWrapper<InventoryDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        queryWrapper.isNull("check_time");
        queryWrapper.ne("result", "SHORTAGE");
        long unchecked = inventoryDetailService.count(queryWrapper);
        if (unchecked > 0) {
            throw new RuntimeException("还有" + unchecked + "项未盘点，无法完成");
        }

        task.setStatus("COMPLETED");
        task.setEndTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        return updateById(task);
    }

    @Transactional
    public boolean cancelTask(Long taskId) {
        InventoryTask task = getById(taskId);
        if (task == null || "COMPLETED".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不允许取消");
        }
        task.setStatus("CANCELLED");
        task.setUpdateTime(LocalDateTime.now());
        return updateById(task);
    }

    public void fillTaskStatistics(InventoryTask task) {
        QueryWrapper<InventoryDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", task.getId());
        long total = inventoryDetailService.count(queryWrapper);

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", task.getId());
        queryWrapper.isNotNull("check_time");
        long checked = inventoryDetailService.count(queryWrapper);

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", task.getId());
        queryWrapper.eq("result", "MATCH");
        long match = inventoryDetailService.count(queryWrapper);

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", task.getId());
        queryWrapper.ne("result", "MATCH");
        queryWrapper.isNotNull("result");
        long diff = inventoryDetailService.count(queryWrapper);

        task.setTotalCount((int) total);
        task.setCheckedCount((int) checked);
        task.setMatchCount((int) match);
        task.setDiffCount((int) diff);
    }

    private String generateTaskNo() {
        return "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
