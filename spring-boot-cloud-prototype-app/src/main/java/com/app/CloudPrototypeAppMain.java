package com.app;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * APP后台服务启动类
 *
 * @author yanlei
 * @since 2026-09-05
 */
@Slf4j
@EnableScheduling
@SpringBootApplication
@MapperScan("com.app.mapper")
public class CloudPrototypeAppMain {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(CloudPrototypeAppMain.class, args);
    }
}
