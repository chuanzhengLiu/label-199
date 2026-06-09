package com.company.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.Asset;
import com.company.asset.entity.InventoryDetail;
import com.company.asset.entity.InventoryTask;
import com.company.asset.mapper.InventoryTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryTaskService extends ServiceImpl<InventoryTaskMapper, InventoryTask> {

    @Autowired
    private AssetService assetService;

    @Autowired
    private InventoryDetailService inventoryDetailService;

    /**
     * 创建盘点任务，并按范围生成账面待盘明细（不修改资产原始台账）
     */
    @Transactional
    public boolean createTask(InventoryTask task) {
        if (task.getTaskNo() == null || task.getTaskNo().isEmpty()) {
            task.setTaskNo("INV-" + System.currentTimeMillis());
        }
        task.setStatus("PENDING");
        task.setNormalCount(0);
        task.setProfitCount(0);
        task.setLossCount(0);
        task.setCheckedCount(0);

        // 按范围筛选账面资产（已报废资产不纳入盘点）
        QueryWrapper<Asset> wrapper = new QueryWrapper<>();
        wrapper.ne("status", "SCRAPPED");
        if ("DEPARTMENT".equals(task.getScopeType())) {
            wrapper.eq("department_id", task.getScopeValue());
        } else if ("LOCATION".equals(task.getScopeType())) {
            wrapper.like("location", task.getScopeValue());
        }
        List<Asset> assets = assetService.list(wrapper);
        task.setTotalCount(assets.size());

        boolean ok = save(task);
        if (!ok) {
            return false;
        }

        // 生成盘点明细
        List<InventoryDetail> details = new ArrayList<>();
        for (Asset asset : assets) {
            InventoryDetail detail = new InventoryDetail();
            detail.setTaskId(task.getId());
            detail.setAssetId(asset.getId());
            detail.setAssetNo(asset.getAssetNo());
            detail.setAssetName(asset.getName());
            detail.setBookStatus(asset.getStatus());
            detail.setBookLocation(asset.getLocation());
            detail.setBookDepartment(asset.getDepartmentId());
            detail.setResult("UNCHECKED");
            details.add(detail);
        }
        if (!details.isEmpty()) {
            inventoryDetailService.saveBatch(details);
        }
        return true;
    }

    /**
     * 开始盘点：把任务置为执行中
     */
    @Transactional
    public boolean startTask(Long taskId) {
        InventoryTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("盘点任务不存在");
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new RuntimeException("当前状态不可开始盘点");
        }
        task.setStatus("IN_PROGRESS");
        task.setStartTime(LocalDateTime.now());
        return updateById(task);
    }

    /**
     * 完成盘点：统计差异并标记完成。
     * 前置校验：所有明细必须已盘点（不存在 UNCHECKED 状态的明细）。
     */
    @Transactional
    public boolean finishTask(Long taskId) {
        InventoryTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("盘点任务不存在");
        }
        if ("COMPLETED".equals(task.getStatus())) {
            throw new RuntimeException("任务已完成");
        }

        QueryWrapper<InventoryDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        List<InventoryDetail> details = inventoryDetailService.list(wrapper);

        // 前置校验：禁止存在未盘点明细
        long unchecked = details.stream()
                .filter(d -> d.getResult() == null || "UNCHECKED".equals(d.getResult()))
                .count();
        if (unchecked > 0) {
            throw new RuntimeException("仍有 " + unchecked + " 项未盘点，无法完成任务");
        }

        int normal = 0, profit = 0, loss = 0, checked = 0;
        for (InventoryDetail d : details) {
            if (!"UNCHECKED".equals(d.getResult())) {
                checked++;
            }
            // MATCH 计入账实相符；STATUS_DIFF / LOCATION_DIFF / BOTH_DIFF / LOSS 计入盘亏类差异
            if ("MATCH".equals(d.getResult())) {
                normal++;
            } else if ("PROFIT".equals(d.getResult())) {
                profit++;
            } else if ("LOSS".equals(d.getResult())
                    || "STATUS_DIFF".equals(d.getResult())
                    || "LOCATION_DIFF".equals(d.getResult())
                    || "BOTH_DIFF".equals(d.getResult())) {
                loss++;
            }
        }

        task.setCheckedCount(checked);
        task.setNormalCount(normal);
        task.setProfitCount(profit);
        task.setLossCount(loss);
        task.setStatus("COMPLETED");
        task.setEndTime(LocalDateTime.now());
        return updateById(task);
    }

    /**
     * 生成差异汇总报告
     */
    public Map<String, Object> generateReport(Long taskId) {
        InventoryTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("盘点任务不存在");
        }

        QueryWrapper<InventoryDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        List<InventoryDetail> all = inventoryDetailService.list(wrapper);

        List<InventoryDetail> diffs = new ArrayList<>();
        for (InventoryDetail d : all) {
            if ("PROFIT".equals(d.getResult())
                    || "LOSS".equals(d.getResult())
                    || "STATUS_DIFF".equals(d.getResult())
                    || "LOCATION_DIFF".equals(d.getResult())
                    || "BOTH_DIFF".equals(d.getResult())) {
                diffs.add(d);
            }
        }

        Map<String, Object> report = new HashMap<>();
        report.put("task", task);
        report.put("totalCount", task.getTotalCount());
        report.put("checkedCount", task.getCheckedCount());
        report.put("normalCount", task.getNormalCount());
        report.put("profitCount", task.getProfitCount());
        report.put("lossCount", task.getLossCount());
        report.put("differences", diffs);
        return report;
    }
}
