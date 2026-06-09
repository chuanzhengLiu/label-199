package com.company.asset.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.asset.common.Result;
import com.company.asset.entity.ScrapRecord;
import com.company.asset.service.ScrapRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scrap")
public class ScrapRecordController {

    @Autowired
    private ScrapRecordService scrapRecordService;

    @GetMapping("/list")
    public Result<Page<ScrapRecord>> list(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(scrapRecordService.page(new Page<>(page, size)));
    }

    @PostMapping("/apply")
    public Result<Boolean> apply(@RequestBody ScrapRecord record) {
        try {
            return Result.success(scrapRecordService.applyScrap(record));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/approve/{id}")
    public Result<Boolean> approve(@PathVariable Long id, @RequestParam boolean approved) {
        try {
            return Result.success(scrapRecordService.approveScrap(id, approved));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
