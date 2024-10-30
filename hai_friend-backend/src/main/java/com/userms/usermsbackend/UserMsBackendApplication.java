package com.userms.usermsbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.userms.usermsbackend.mapper")
@EnableScheduling
public class UserMsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserMsBackendApplication.class, args);
    }

}
