package com.company.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.InventoryDetail;
import com.company.asset.mapper.InventoryDetailMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryDetailService extends ServiceImpl<InventoryDetailMapper, InventoryDetail> {

    @Transactional
    public boolean checkAsset(InventoryDetail detail) {
        InventoryDetail existing = getById(detail.getId());
        if (existing == null) {
            throw new RuntimeException("盘点明细不存在");
        }

        existing.setActualStatus(detail.getActualStatus());
        existing.setActualLocation(detail.getActualLocation());
        existing.setRemarks(detail.getRemarks());
        existing.setCheckTime(LocalDateTime.now());
        existing.setUpdateTime(LocalDateTime.now());
        existing.setResult(calculateResult(existing));
        return updateById(existing);
    }

    @Transactional
    public boolean markShortage(Long detailId, String remarks) {
        InventoryDetail existing = getById(detailId);
        if (existing == null) {
            throw new RuntimeException("盘点明细不存在");
        }
        if (existing.getCheckTime() != null) {
            throw new RuntimeException("该资产已盘点，无法标记盘亏");
        }
        existing.setActualStatus("MISSING");
        existing.setResult("SHORTAGE");
        existing.setRemarks(remarks);
        existing.setUpdateTime(LocalDateTime.now());
        return updateById(existing);
    }

    @Transactional
    public InventoryDetail addSurplusAsset(InventoryDetail detail) {
        detail.setResult("SURPLUS");
        detail.setCheckTime(LocalDateTime.now());
        detail.setCreateTime(LocalDateTime.now());
        detail.setUpdateTime(LocalDateTime.now());
        save(detail);
        return detail;
    }

    public Map<String, Object> getTaskReport(Long taskId) {
        QueryWrapper<InventoryDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        List<InventoryDetail> allDetails = list(queryWrapper);

        long totalCount = allDetails.size();
        long checkedCount = allDetails.stream().filter(d -> d.getCheckTime() != null).count();
        long matchCount = allDetails.stream().filter(d -> "MATCH".equals(d.getResult())).count();
        long surplusCount = allDetails.stream().filter(d -> "SURPLUS".equals(d.getResult())).count();
        long shortageCount = allDetails.stream().filter(d -> "SHORTAGE".equals(d.getResult())).count();
        long statusMismatchCount = allDetails.stream().filter(d -> "STATUS_MISMATCH".equals(d.getResult())).count();
        long locationMismatchCount = allDetails.stream().filter(d -> "LOCATION_MISMATCH".equals(d.getResult())).count();

        long bothMismatchCount = allDetails.stream().filter(d -> "BOTH_MISMATCH".equals(d.getResult())).count();

        List<InventoryDetail> diffDetails = allDetails.stream()
                .filter(d -> d.getResult() != null && !"MATCH".equals(d.getResult()))
                .collect(Collectors.toList());

        Map<String, Object> report = Map.of(
                "totalCount", totalCount,
                "checkedCount", checkedCount,
                "matchCount", matchCount,
                "surplusCount", surplusCount,
                "shortageCount", shortageCount,
                "statusMismatchCount", statusMismatchCount,
                "locationMismatchCount", locationMismatchCount,
                "bothMismatchCount", bothMismatchCount,
                "diffDetails", diffDetails
        );

        return report;
    }

    private String calculateResult(InventoryDetail detail) {
        boolean statusMatch = detail.getBookStatus() == null || detail.getBookStatus().equals(detail.getActualStatus());
        boolean locationMatch = detail.getBookLocation() == null || detail.getBookLocation().equals(detail.getActualLocation());

        if (statusMatch && locationMatch) {
            return "MATCH";
        } else if (!statusMatch && !locationMatch) {
            return "BOTH_MISMATCH";
        } else if (!statusMatch) {
            return "STATUS_MISMATCH";
        } else {
            return "LOCATION_MISMATCH";
        }
    }
}
