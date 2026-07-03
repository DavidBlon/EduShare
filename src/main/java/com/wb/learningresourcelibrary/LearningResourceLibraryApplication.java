package com.wb.learningresourcelibrary;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 教育资源共享平台 - 启动类
 */
@SpringBootApplication
@MapperScan(basePackages = "com.wb.learningresourcelibrary.mapper",
            sqlSessionFactoryRef = "sqlSessionFactory")
public class LearningResourceLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningResourceLibraryApplication.class, args);
    }

}
