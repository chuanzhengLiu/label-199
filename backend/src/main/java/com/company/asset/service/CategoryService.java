package com.company.asset.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.Category;
import com.company.asset.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {
}
