package com.company.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("inventory_detail")
public class InventoryDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long assetId;
    private String assetNo;
    private String assetName;
    private String bookStatus;
    private String bookLocation;
    private String bookDepartmentId;
    private String actualStatus;
    private String actualLocation;
    private String checkResult;
    private Integer isChecked;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;
    
    private Long checkerId;
    private String checkerName;
    private String remark;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
