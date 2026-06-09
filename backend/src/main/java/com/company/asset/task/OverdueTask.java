package com.company.asset.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.asset.entity.BorrowRecord;
import com.company.asset.service.BorrowRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OverdueTask {

    @Autowired
    private BorrowRecordService borrowRecordService;

    // Run every day at 9 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkOverdue() {
        log.info("Checking for overdue assets...");
        QueryWrapper<BorrowRecord> query = new QueryWrapper<>();
        query.eq("status", "BORROWED");
        query.lt("return_date", LocalDateTime.now());
        
        List<BorrowRecord> overdueRecords = borrowRecordService.list(query);
        for (BorrowRecord record : overdueRecords) {
            // In a real app, send email or notification here
            log.warn("Overdue Alert: Asset ID {} borrowed by User ID {} was due on {}", 
                    record.getAssetId(), record.getUserId(), record.getReturnDate());
        }
    }
}
