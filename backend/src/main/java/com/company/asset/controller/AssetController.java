package com.company.asset.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.asset.common.Result;
import com.company.asset.entity.Asset;
import com.company.asset.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asset")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @GetMapping("/list")
    public Result<Page<Asset>> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) String departmentId,
                                    @RequestParam(required = false) String status) {
        Page<Asset> pageParam = new Page<>(page, size);
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        if (categoryId != null) {
            queryWrapper.eq("category_id", categoryId);
        }
        if (departmentId != null && !departmentId.isEmpty()) {
            queryWrapper.like("department_id", departmentId);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        return Result.success(assetService.page(pageParam, queryWrapper));
    }

    @PostMapping("/add")
    public Result<Boolean> save(@RequestBody Asset asset) {
        return Result.success(assetService.saveOrUpdate(asset));
    }
    
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody Asset asset) {
        return Result.success(assetService.updateById(asset));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(assetService.removeById(id));
    }
}
