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
public class InventoryController {

    @Autowired
    private InventoryTaskService inventoryTaskService;

    @Autowired
    private InventoryDetailService inventoryDetailService;

    @GetMapping("/list")
    public Result<Page<InventoryTask>> list(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) Long assigneeId,
                                            @RequestParam(required = false) String status) {
        Page<InventoryTask> pageParam = new Page<>(page, size);
        QueryWrapper<InventoryTask> wrapper = new QueryWrapper<>();
        if (assigneeId != null) {
            wrapper.eq("assignee_id", assigneeId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return Result.success(inventoryTaskService.page(pageParam, wrapper));
    }

    @PostMapping("/create")
    public Result<Boolean> create(@RequestBody InventoryTask task) {
        try {
            return Result.success(inventoryTaskService.createTask(task));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/start/{id}")
    public Result<Boolean> start(@PathVariable Long id) {
        try {
            return Result.success(inventoryTaskService.startTask(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/finish/{id}")
    public Result<Boolean> finish(@PathVariable Long id) {
        try {
            return Result.success(inventoryTaskService.finishTask(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail/list")
    public Result<List<InventoryDetail>> detailList(@RequestParam Long taskId) {
        QueryWrapper<InventoryDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId).orderByAsc("id");
        return Result.success(inventoryDetailService.list(wrapper));
    }

    @PostMapping("/detail/check")
    public Result<Boolean> check(@RequestBody InventoryDetail detail) {
        try {
            return Result.success(inventoryDetailService.checkAsset(detail));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/detail/profit")
    public Result<Boolean> addProfit(@RequestBody InventoryDetail detail) {
        try {
            return Result.success(inventoryDetailService.addProfit(detail));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/report/{id}")
    public Result<Map<String, Object>> report(@PathVariable Long id) {
        try {
            return Result.success(inventoryTaskService.generateReport(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
