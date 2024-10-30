package com.userms.usermsbackend.service;
import java.util.Date;

import com.userms.usermsbackend.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class redisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增
        valueOperations.set("redisTestString", "zhangsan");
        valueOperations.set("redisTestInt", 1);
        valueOperations.set("redisTestDouble", 2.0);
        User user = new User();
        user.setId(1);
        user.setUsername("haiy");
        valueOperations.set("redisTestUser", user);
        //查
        Object redisTestString = valueOperations.get("redisTestString");
        Assertions.assertTrue("zhangsan".equals((String)redisTestString));
        Object redisTestInt = valueOperations.get("redisTestInt");
        Assertions.assertTrue(1 == (int) redisTestInt);
        Object redisTestDouble = valueOperations.get("redisTestDouble");
        Assertions.assertEquals(2.0, (double)redisTestDouble);
        Object redisTestUser = valueOperations.get("redisTestUser");
        Assertions.assertTrue(user.equals(redisTestUser));
        //改
        //怎加重复的值就是改
        valueOperations.set("redisTestString", "lisi");
        //删
        redisTemplate.delete("redisTestString");
        redisTemplate.delete("redisTestInt");
        redisTemplate.delete("redisTestDouble");
        redisTemplate.delete("redisTestUser");
    }
}
