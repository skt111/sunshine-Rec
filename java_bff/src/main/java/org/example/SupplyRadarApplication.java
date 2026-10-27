package org.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 供给端雷达应用启动类。
 */
@SpringBootApplication
@MapperScan({"org.example.supplyradar.mapper", "org.example.socialbrand.mapper"})
@EnableScheduling
@EnableAsync
public class SupplyRadarApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupplyRadarApplication.class, args);
    }
}
