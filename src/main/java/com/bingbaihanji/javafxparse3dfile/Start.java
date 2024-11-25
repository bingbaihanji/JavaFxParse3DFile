package com.bingbaihanji.javafxparse3dfile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author 冰白寒祭
 * @date 2024-11-11 15:36:10
 * @description //TODO
 */

@SpringBootApplication
public class Start {
    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        ConfigurableApplicationContext context = SpringApplication.run(Start.class, args);
        context.start();
        Main.main(args);
    }
}