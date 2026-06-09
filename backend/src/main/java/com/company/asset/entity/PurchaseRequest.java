package com.company.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("purchase_request")
public class PurchaseRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String categoryName;
    private Integer quantity;
    private String remarks;
    private String status; // PENDING, APPROVED, REJECTED
    private Long applicantId;
    private String applicantName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
