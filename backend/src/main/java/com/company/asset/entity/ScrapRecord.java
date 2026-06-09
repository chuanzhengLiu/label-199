package com.company.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scrap_record")
public class ScrapRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private String reason;
    private Long approvedBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scrapDate;
    
    private String status; // PENDING, APPROVED, REJECTED
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
