package com.company.asset.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.asset.common.Result;
import com.company.asset.entity.Category;
import com.company.asset.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }
}
