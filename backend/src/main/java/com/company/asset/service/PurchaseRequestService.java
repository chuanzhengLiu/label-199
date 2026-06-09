package com.company.asset.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.PurchaseRequest;
import com.company.asset.mapper.PurchaseRequestMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PurchaseRequestService extends ServiceImpl<PurchaseRequestMapper, PurchaseRequest> {

    public boolean apply(PurchaseRequest request) {
        request.setCreateTime(LocalDateTime.now());
        request.setStatus("PENDING");
        return save(request);
    }
}
