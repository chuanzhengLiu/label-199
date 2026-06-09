package com.company.asset.controller;

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
public class InventoryController {

    @Autowired
    private InventoryTaskService inventoryTaskService;

    @Autowired
    private InventoryDetailService inventoryDetailService;

    @GetMapping("/task/list")
    public Result<Page<InventoryTask>> taskList(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) String taskName,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) Long executorId) {
        return Result.success(inventoryTaskService.getTaskPage(page, size, taskName, status, executorId));
    }

    @GetMapping("/task/{id}")
    public Result<InventoryTask> getTask(@PathVariable Long id) {
        InventoryTask task = inventoryTaskService.getById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        return Result.success(task);
    }

    @PostMapping("/task/create")
    public Result<Boolean> createTask(@RequestBody InventoryTask task) {
        if (task.getTaskName() == null || task.getTaskName().isEmpty()) {
            return Result.error("任务名称不能为空");
        }
        if (task.getExecutorId() == null) {
            return Result.error("请选择执行人");
        }
        if ((!"ALL".equals(task.getTaskType())) && (task.getTargetValue() == null || task.getTargetValue().isEmpty())) {
            return Result.error("请输入盘点范围");
        }
        boolean result = inventoryTaskService.createTask(task);
        if (!result) {
            return Result.error("创建任务失败");
        }
        return Result.success(true);
    }

    @PostMapping("/task/start/{id}")
    public Result<Boolean> startTask(@PathVariable Long id) {
        boolean result = inventoryTaskService.startTask(id);
        if (!result) {
            return Result.error("任务不存在");
        }
        return Result.success(true);
    }

    @PostMapping("/task/complete/{id}")
    public Result<Boolean> completeTask(@PathVariable Long id) {
        InventoryTask task = inventoryTaskService.getById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        boolean result = inventoryTaskService.completeTask(id);
        if (!result) {
            return Result.error("存在未盘点的资产，无法完成盘点");
        }
        return Result.success(true);
    }

    @DeleteMapping("/task/{id}")
    public Result<Boolean> deleteTask(@PathVariable Long id) {
        boolean result = inventoryTaskService.deleteTask(id);
        if (!result) {
            return Result.error("删除失败");
        }
        return Result.success(true);
    }

    @GetMapping("/detail/list/{taskId}")
    public Result<List<InventoryDetail>> detailList(@PathVariable Long taskId,
                                                    @RequestParam(required = false) String checkResult,
                                                    @RequestParam(required = false) Integer isChecked) {
        return Result.success(inventoryDetailService.getDetailList(taskId, checkResult, isChecked));
    }

    @PostMapping("/detail/check")
    public Result<Boolean> checkDetail(@RequestBody InventoryDetail detail) {
        if (detail.getId() == null || detail.getTaskId() == null) {
            return Result.error("参数错误");
        }
        boolean result = inventoryDetailService.checkDetail(detail);
        if (!result) {
            return Result.error("盘点失败");
        }
        return Result.success(true);
    }

    @PostMapping("/detail/mark-loss")
    public Result<Boolean> markLoss(@RequestParam Long taskId, @RequestParam Long detailId) {
        if (taskId == null || detailId == null) {
            return Result.error("参数错误");
        }
        boolean result = inventoryDetailService.markLoss(taskId, detailId);
        if (!result) {
            return Result.error("操作失败");
        }
        return Result.success(true);
    }

    @PostMapping("/detail/add-profit")
    public Result<Boolean> addProfit(@RequestBody InventoryDetail detail) {
        if (detail.getTaskId() == null) {
            return Result.error("参数错误");
        }
        if (detail.getAssetNo() == null || detail.getAssetNo().isEmpty()) {
            return Result.error("资产编号不能为空");
        }
        if (detail.getAssetName() == null || detail.getAssetName().isEmpty()) {
            return Result.error("资产名称不能为空");
        }
        boolean result = inventoryDetailService.addProfit(detail);
        if (!result) {
            return Result.error("添加失败");
        }
        return Result.success(true);
    }

    @GetMapping("/report/{taskId}")
    public Result<Map<String, Object>> getReport(@PathVariable Long taskId) {
        Map<String, Object> report = inventoryTaskService.getReport(taskId);
        if (report == null) {
            return Result.error("任务不存在");
        }
        return Result.success(report);
    }

    @GetMapping("/detail/scan/{taskId}")
    public Result<InventoryDetail> scanAsset(@PathVariable Long taskId, @RequestParam String assetNo) {
        if (assetNo == null || assetNo.isEmpty()) {
            return Result.error("资产编号不能为空");
        }
        InventoryDetail detail = inventoryDetailService.scanAsset(taskId, assetNo);
        if (detail == null) {
            return Result.error("未找到该资产");
        }
        return Result.success(detail);
    }
}
