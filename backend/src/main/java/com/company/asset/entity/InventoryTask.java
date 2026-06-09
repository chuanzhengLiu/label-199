package com.company.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    private String title;
    private String departmentId;
    private String location;
    private Long assigneeId;
    private String assigneeName;
    private String status; // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    private Long creatorId;
    private String creatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Integer totalCount;

    @TableField(exist = false)
    private Integer checkedCount;

    @TableField(exist = false)
    private Integer matchCount;

    @TableField(exist = false)
    private Integer diffCount;
}
