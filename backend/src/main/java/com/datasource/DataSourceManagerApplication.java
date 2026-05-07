package com.datasource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.datasource.mapper")
@EnableScheduling
public class DataSourceManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataSourceManagerApplication.class, args);
    }
}
