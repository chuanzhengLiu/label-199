package com.company.asset.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.asset.common.Result;
import com.company.asset.entity.InventoryItem;
import com.company.asset.service.InventoryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inventory/item")
public class InventoryItemController {

    @Autowired
    private InventoryItemService inventoryItemService;

    @GetMapping("/list/{taskId}")
    public Result<Page<InventoryItem>> list(@PathVariable Long taskId,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "20") Integer size,
                                            @RequestParam(required = false) String resultType) {
        Page<InventoryItem> pageParam = new Page<>(page, size);
        return Result.success(inventoryItemService.getTaskItemsPage(pageParam, taskId, resultType));
    }

    @PostMapping("/check")
    public Result<InventoryItem> check(@RequestBody InventoryItem item) {
        return Result.success(inventoryItemService.checkItem(item));
    }

    @PostMapping("/surplus/{taskId}")
    public Result<InventoryItem> addSurplus(@PathVariable Long taskId, @RequestBody InventoryItem item) {
        return Result.success(inventoryItemService.addSurplusItem(taskId, item));
    }

    @GetMapping("/report/{taskId}")
    public Result<Map<String, Object>> report(@PathVariable Long taskId) {
        return Result.success(inventoryItemService.getReport(taskId));
    }
}
