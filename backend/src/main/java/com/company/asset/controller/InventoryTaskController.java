package com.company.asset.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.asset.common.Result;
import com.company.asset.entity.InventoryDetail;
import com.company.asset.entity.InventoryTask;
import com.company.asset.service.InventoryDetailService;
import com.company.asset.service.InventoryTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryTaskController {

    @Autowired
    private InventoryTaskService inventoryTaskService;

    @Autowired
    private InventoryDetailService inventoryDetailService;

    @GetMapping("/task/list")
    public Result<Page<InventoryTask>> taskList(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) Long assigneeId) {
        Page<InventoryTask> pageParam = new Page<>(page, size);
        QueryWrapper<InventoryTask> queryWrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        if (assigneeId != null) {
            queryWrapper.eq("assignee_id", assigneeId);
        }
        queryWrapper.orderByDesc("create_time");

        Page<InventoryTask> resultPage = inventoryTaskService.page(pageParam, queryWrapper);
        resultPage.getRecords().forEach(inventoryTaskService::fillTaskStatistics);

        return Result.success(resultPage);
    }

    @PostMapping("/task/add")
    public Result<InventoryTask> createTask(@RequestBody InventoryTask task) {
        try {
            return Result.success(inventoryTaskService.createTask(task));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/task/start/{id}")
    public Result<Boolean> startTask(@PathVariable Long id) {
        try {
            return Result.success(inventoryTaskService.startTask(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/task/complete/{id}")
    public Result<Boolean> completeTask(@PathVariable Long id) {
        try {
            return Result.success(inventoryTaskService.completeTask(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/task/cancel/{id}")
    public Result<Boolean> cancelTask(@PathVariable Long id) {
        try {
            return Result.success(inventoryTaskService.cancelTask(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail/list")
    public Result<Page<InventoryDetail>> detailList(@RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size,
                                                     @RequestParam Long taskId,
                                                     @RequestParam(required = false) String result) {
        Page<InventoryDetail> pageParam = new Page<>(page, size);
        QueryWrapper<InventoryDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        if (result != null && !result.isEmpty()) {
            queryWrapper.eq("result", result);
        }
        queryWrapper.orderByAsc("asset_no");

        return Result.success(inventoryDetailService.page(pageParam, queryWrapper));
    }

    @GetMapping("/detail/all")
    public Result<List<InventoryDetail>> detailAll(@RequestParam Long taskId) {
        QueryWrapper<InventoryDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        queryWrapper.orderByAsc("asset_no");
        return Result.success(inventoryDetailService.list(queryWrapper));
    }

    @PostMapping("/detail/check")
    public Result<Boolean> checkAsset(@RequestBody InventoryDetail detail) {
        try {
            return Result.success(inventoryDetailService.checkAsset(detail));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/detail/surplus")
    public Result<InventoryDetail> addSurplusAsset(@RequestBody InventoryDetail detail) {
        try {
            return Result.success(inventoryDetailService.addSurplusAsset(detail));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/detail/shortage/{id}")
    public Result<Boolean> markShortage(@PathVariable Long id, @RequestParam(required = false) String remarks) {
        try {
            return Result.success(inventoryDetailService.markShortage(id, remarks));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/report")
    public Result<Map<String, Object>> report(@RequestParam Long taskId) {
        return Result.success(inventoryDetailService.getTaskReport(taskId));
    }
}
