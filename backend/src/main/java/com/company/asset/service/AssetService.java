package com.company.asset.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.Asset;
import com.company.asset.mapper.AssetMapper;
import org.springframework.stereotype.Service;

@Service
public class AssetService extends ServiceImpl<AssetMapper, Asset> {
}
