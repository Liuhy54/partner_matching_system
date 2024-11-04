package com.hai_friend.hai_friend_backend.service;

import com.hai_friend.hai_friend_backend.model.enums.teamStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class teamTest {


    @Test
    public void test() {
        teamStatusEnum statusEnum = teamStatusEnum.getEnumByValue(1);
        System.out.println(statusEnum);
        System.out.println(statusEnum.getValue());
        System.out.println(statusEnum.getText());
        System.out.println(statusEnum.name());
    }
}
