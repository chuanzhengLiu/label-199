package com.company.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("asset")
public class Asset {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String assetNo;
    private String name;
    private Long categoryId;
    private String model;
    private String serialNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;
    
    private String supplier;
    private BigDecimal price;
    private Integer usefulLife;
    private String status; // NORMAL, BORROWED, MAINTENANCE, SCRAPPED
    private String location;
    private String departmentId;
    private Long currentUserId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
