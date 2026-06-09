package com.company.asset.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.asset.common.Result;
import com.company.asset.entity.InventoryTask;
import com.company.asset.service.InventoryTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory/task")
public class InventoryTaskController {

    @Autowired
    private InventoryTaskService inventoryTaskService;

    @GetMapping("/list")
    public Result<Page<InventoryTask>> list(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) Long assigneeId) {
        Page<InventoryTask> pageParam = new Page<>(page, size);
        return Result.success(inventoryTaskService.pageList(pageParam, status, assigneeId));
    }

    @GetMapping("/{id}")
    public Result<InventoryTask> getById(@PathVariable Long id) {
        return Result.success(inventoryTaskService.getById(id));
    }

    @PostMapping("/create")
    public Result<InventoryTask> create(@RequestBody InventoryTask task) {
        return Result.success(inventoryTaskService.createTask(task));
    }

    @PostMapping("/start/{id}")
    public Result<Boolean> start(@PathVariable Long id) {
        return Result.success(inventoryTaskService.startTask(id));
    }

    @PostMapping("/complete/{id}")
    public Result<Boolean> complete(@PathVariable Long id) {
        int result = inventoryTaskService.completeTask(id);
        if (result == 0) {
            return Result.success(true);
        } else if (result > 0) {
            return Result.error("还有 " + result + " 项资产未盘点，无法完成任务");
        } else {
            return Result.error("任务状态不正确");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(inventoryTaskService.removeById(id));
    }
}
