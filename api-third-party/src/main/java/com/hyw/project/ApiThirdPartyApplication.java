package com.hyw.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author hyw
 */
@SpringBootApplication
@MapperScan("com.hyw.project.mapper")
public class ApiThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiThirdPartyApplication.class, args);
    }

}
