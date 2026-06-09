package com.company.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.asset.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
