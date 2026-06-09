package com.company.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("inventory_task")
public class InventoryTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskNo;
    private String taskName;
    private String scopeType;
    private String scopeValue;
    private Long assigneeId;
    private String assigneeName;
    private String status;
    private Integer totalCount;
    private Integer checkedCount;
    private Integer surplusCount;
    private Integer lossCount;
    private Integer differenceCount;
    private String remarks;
    private Long createBy;
    private String createName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;
}
