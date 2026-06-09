package com.company.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.Asset;
import com.company.asset.entity.InventoryDetail;
import com.company.asset.entity.InventoryTask;
import com.company.asset.mapper.InventoryDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryDetailService extends ServiceImpl<InventoryDetailMapper, InventoryDetail> {

    @Autowired
    private AssetService assetService;

    @Autowired
    @Lazy
    private InventoryTaskService inventoryTaskService;

    /**
     * 现场核对：根据账面与实物比对，生成盘点结论。
     * 区分五种结果：MATCH-账实相符, STATUS_DIFF-仅状态不同, LOCATION_DIFF-仅位置不同,
     * BOTH_DIFF-状态与位置均不同, LOSS-盘亏。
     * 仅记录盘点结果，不修改资产原始台账数据。
     */
    @Transactional
    public boolean checkAsset(InventoryDetail input) {
        InventoryDetail detail = getById(input.getId());
        if (detail == null) {
            throw new RuntimeException("盘点明细不存在");
        }

        InventoryTask task = inventoryTaskService.getById(detail.getTaskId());
        if (task == null || "COMPLETED".equals(task.getStatus())) {
            throw new RuntimeException("任务不可盘点");
        }

        detail.setActualStatus(input.getActualStatus());
        detail.setActualLocation(input.getActualLocation());
        detail.setRemark(input.getRemark());
        detail.setCheckerId(input.getCheckerId());
        detail.setCheckTime(LocalDateTime.now());

        // 若调用方显式标记盘亏，则直接置为 LOSS
        if ("LOSS".equals(input.getResult())) {
            detail.setResult("LOSS");
            return updateById(detail);
        }

        // 实物未提供任何信息视为盘亏
        boolean actualStatusEmpty = isEmpty(input.getActualStatus());
        boolean actualLocationEmpty = isEmpty(input.getActualLocation());
        if (actualStatusEmpty && actualLocationEmpty) {
            detail.setResult("LOSS");
            return updateById(detail);
        }

        // 比对账面与实物
        boolean statusMatch = equalsIgnoreNull(detail.getBookStatus(), input.getActualStatus());
        boolean locationMatch = equalsIgnoreNull(detail.getBookLocation(), input.getActualLocation());

        if (statusMatch && locationMatch) {
            detail.setResult("MATCH");
        } else if (!statusMatch && !locationMatch) {
            detail.setResult("BOTH_DIFF");
        } else if (!statusMatch) {
            detail.setResult("STATUS_DIFF");
        } else {
            detail.setResult("LOCATION_DIFF");
        }
        return updateById(detail);
    }

    /**
     * 现场登记盘盈资产：账面无该资产，实物存在
     */
    @Transactional
    public boolean addProfit(InventoryDetail input) {
        InventoryTask task = inventoryTaskService.getById(input.getTaskId());
        if (task == null || "COMPLETED".equals(task.getStatus())) {
            throw new RuntimeException("任务不可盘点");
        }
        // 通过资产编号校验是否已在账面（盘盈即账面外资产）
        if (input.getAssetNo() != null && !input.getAssetNo().isEmpty()) {
            QueryWrapper<Asset> aw = new QueryWrapper<>();
            aw.eq("asset_no", input.getAssetNo());
            Asset exist = assetService.getOne(aw);
            if (exist != null) {
                throw new RuntimeException("该资产编号已在账面，请直接核对");
            }
        }
        input.setResult("PROFIT");
        input.setCheckTime(LocalDateTime.now());
        return save(input);
    }

    private boolean equalsIgnoreNull(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        return a.equals(b);
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
