package com.company.asset.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.asset.common.Result;
import com.company.asset.entity.Asset;
import com.company.asset.entity.Category;
import com.company.asset.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
public class StatisticsController {

    @Autowired
    private AssetService assetService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BorrowRecordService borrowRecordService;

    @Autowired
    private MaintenanceRecordService maintenanceRecordService;

    @Autowired
    private ScrapRecordService scrapRecordService;

    @GetMapping("/dashboard")
    public Result<Map<String, Long>> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalAssets", assetService.count());
        
        QueryWrapper<Asset> borrowedWrapper = new QueryWrapper<>();
        borrowedWrapper.eq("status", "BORROWED");
        stats.put("borrowedAssets", assetService.count(borrowedWrapper));

        QueryWrapper<Asset> maintenanceWrapper = new QueryWrapper<>();
        maintenanceWrapper.eq("status", "MAINTENANCE");
        stats.put("maintenanceAssets", assetService.count(maintenanceWrapper));

        QueryWrapper<Asset> scrapWrapper = new QueryWrapper<>();
        scrapWrapper.eq("status", "SCRAPPED");
        stats.put("scrappedAssets", assetService.count(scrapWrapper));
        
        return Result.success(stats);
    }

    @GetMapping("/low-stock")
    public Result<List<Map<String, Object>>> getLowStockStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Get all categories
        List<Category> categories = categoryService.list();
        
        // Count available (NORMAL) assets per category
        // In a larger system, we'd do this with a custom SQL query, but for now loop is fine
        for (Category category : categories) {
            QueryWrapper<Asset> query = new QueryWrapper<>();
            query.eq("category_id", category.getId());
            query.eq("status", "NORMAL");
            long count = assetService.count(query);
            
            // Threshold for low stock, e.g., 5
            if (count < 5) {
                Map<String, Object> item = new HashMap<>();
                item.put("categoryId", category.getId());
                item.put("categoryName", category.getName());
                item.put("count", count);
                result.add(item);
            }
        }
        
        return Result.success(result);
    }
    
    @GetMapping("/category-distribution")
    public Result<List<Map<String, Object>>> getCategoryDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Category> categories = categoryService.list();
        
        for (Category category : categories) {
            QueryWrapper<Asset> query = new QueryWrapper<>();
            query.eq("category_id", category.getId());
            long count = assetService.count(query);
            
            Map<String, Object> item = new HashMap<>();
            item.put("categoryName", category.getName()); // Consistent key for frontend
            item.put("count", count);
            result.add(item);
        }
        
        return Result.success(result);
    }
}
