package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // Spring Boot的核心注解，用于开启自动配置
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args); // 启动Spring Boot应用
    }

}