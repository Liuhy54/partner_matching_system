package com.hai_friend.hai_friend_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.hai_friend.hai_friend_backend.mapper")
@EnableScheduling
public class HaiFriendBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HaiFriendBackendApplication.class, args);
    }

}
