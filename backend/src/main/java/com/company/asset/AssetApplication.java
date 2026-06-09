package com.company.asset;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.company.asset.mapper")
@EnableScheduling
public class AssetApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssetApplication.class, args);
    }
}
