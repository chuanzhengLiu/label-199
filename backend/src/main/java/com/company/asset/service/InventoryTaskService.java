package com.company.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.Asset;
import com.company.asset.entity.InventoryDetail;
import com.company.asset.entity.InventoryTask;
import com.company.asset.entity.User;
import com.company.asset.mapper.InventoryTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryTaskService extends ServiceImpl<InventoryTaskMapper, InventoryTask> {

    @Autowired
    private InventoryDetailService inventoryDetailService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private UserService userService;

    public Page<InventoryTask> getTaskPage(Integer page, Integer size, String taskName, String status, Long executorId) {
        Page<InventoryTask> pageParam = new Page<>(page, size);
        QueryWrapper<InventoryTask> queryWrapper = new QueryWrapper<>();
        if (taskName != null && !taskName.isEmpty()) {
            queryWrapper.like("task_name", taskName);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        if (executorId != null) {
            queryWrapper.eq("executor_id", executorId);
        }
        queryWrapper.orderByDesc("create_time");
        return page(pageParam, queryWrapper);
    }

    @Transactional
    public boolean createTask(InventoryTask task) {
        User executor = userService.getById(task.getExecutorId());
        if (executor != null) {
            task.setExecutorName(executor.getRealName());
        }
        task.setStatus("PENDING");
        task.setCheckedCount(0);
        task.setProfitCount(0);
        task.setLossCount(0);
        task.setCreateTime(LocalDateTime.now());
        
        QueryWrapper<Asset> assetQuery = new QueryWrapper<>();
        if ("DEPARTMENT".equals(task.getTaskType()) && task.getTargetValue() != null && !task.getTargetValue().isEmpty()) {
            assetQuery.eq("department_id", task.getTargetValue());
        } else if ("LOCATION".equals(task.getTaskType()) && task.getTargetValue() != null && !task.getTargetValue().isEmpty()) {
            assetQuery.eq("location", task.getTargetValue());
        }
        assetQuery.ne("status", "SCRAPPED");
        
        List<Asset> assets = assetService.list(assetQuery);
        task.setTotalCount(assets.size());
        
        save(task);
        
        for (Asset asset : assets) {
            InventoryDetail detail = new InventoryDetail();
            detail.setTaskId(task.getId());
            detail.setAssetId(asset.getId());
            detail.setAssetNo(asset.getAssetNo());
            detail.setAssetName(asset.getName());
            detail.setBookStatus(asset.getStatus());
            detail.setBookLocation(asset.getLocation());
            detail.setBookDepartmentId(asset.getDepartmentId());
            detail.setIsChecked(0);
            inventoryDetailService.save(detail);
        }
        
        return true;
    }

    public boolean startTask(Long id) {
        InventoryTask task = getById(id);
        if (task == null) {
            return false;
        }
        task.setStatus("IN_PROGRESS");
        task.setStartTime(LocalDateTime.now());
        return updateById(task);
    }

    public boolean completeTask(Long id) {
        InventoryTask task = getById(id);
        if (task == null) {
            return false;
        }
        QueryWrapper<InventoryDetail> detailWrapper = new QueryWrapper<>();
        detailWrapper.eq("task_id", id);
        detailWrapper.eq("is_checked", 0);
        long uncheckedCount = inventoryDetailService.count(detailWrapper);
        if (uncheckedCount > 0) {
            return false;
        }
        task.setStatus("COMPLETED");
        task.setEndTime(LocalDateTime.now());
        return updateById(task);
    }

    @Transactional
    public boolean deleteTask(Long id) {
        QueryWrapper<InventoryDetail> detailWrapper = new QueryWrapper<>();
        detailWrapper.eq("task_id", id);
        inventoryDetailService.remove(detailWrapper);
        return removeById(id);
    }

    public Map<String, Object> getReport(Long taskId) {
        InventoryTask task = getById(taskId);
        if (task == null) {
            return null;
        }
        
        QueryWrapper<InventoryDetail> detailWrapper = new QueryWrapper<>();
        detailWrapper.eq("task_id", taskId);
        List<InventoryDetail> details = inventoryDetailService.list(detailWrapper);
        
        Map<String, Object> report = new HashMap<>();
        report.put("task", task);
        report.put("details", details);
        
        long normalCount = details.stream().filter(d -> "NORMAL".equals(d.getCheckResult()) && d.getIsChecked() == 1).count();
        long profitCount = details.stream().filter(d -> "PROFIT".equals(d.getCheckResult())).count();
        long lossCount = details.stream().filter(d -> "LOSS".equals(d.getCheckResult())).count();
        long statusDiffCount = details.stream().filter(d -> "STATUS_DIFF".equals(d.getCheckResult())).count();
        long locationDiffCount = details.stream().filter(d -> "LOCATION_DIFF".equals(d.getCheckResult())).count();
        long bothDiffCount = details.stream().filter(d -> "BOTH_DIFF".equals(d.getCheckResult())).count();
        long uncheckedCount = details.stream().filter(d -> d.getIsChecked() == null || d.getIsChecked() == 0).count();
        
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("normalCount", normalCount);
        statistics.put("profitCount", profitCount);
        statistics.put("lossCount", lossCount);
        statistics.put("statusDiffCount", statusDiffCount);
        statistics.put("locationDiffCount", locationDiffCount);
        statistics.put("bothDiffCount", bothDiffCount);
        statistics.put("uncheckedCount", uncheckedCount);
        report.put("statistics", statistics);
        
        return report;
    }
}
