package com.company.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.asset.entity.InventoryItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryItemMapper extends BaseMapper<InventoryItem> {
}
