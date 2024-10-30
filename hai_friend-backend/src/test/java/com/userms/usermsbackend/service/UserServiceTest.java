package com.userms.usermsbackend.service;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Date;

import com.userms.usermsbackend.model.domain.User;
import jakarta.annotation.Resource;
import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 *
 * @author hy
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser(){
        User user = new User();
        user.setUsername("haiY");
        user.setUserAccount("123");
        user.setAvatarUrl("http://haidati.l-hy.site/picture/3869b3f38f4323784f19bf8f7346643420bc22234f83-EzQ5H6.png");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

//    @Test
//    void userRegister() {
//
//        String userAccount = "haiy3";
//        String userPassword = "";
//        String checkPassword = "12345678";
//        String planetCode = "1";
//        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        assertEquals(-1, result, "密码为空时应返回-1");
//
//        userAccount = "hy";
//        userPassword = "12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        assertEquals(-1, result,"用户名不足4位时应返回-1");
//
//        userPassword = "123456";
//        checkPassword = "123456";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        assertEquals(-1, result,"密码不足8位时应返回-1");
//
//        userAccount = "haiY2";
//        userPassword = "12345678";
//        checkPassword = "12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        assertEquals(-1, result, "用户名重复时应返回-1");
//
//        userAccount = "ha iy3";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        assertEquals(-1, result, "用户名有特殊字符时应返回-1");
//
//        userAccount = "HAIY4";
//        checkPassword = "123456789";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, result, "密码和校验码不一致时应返回-1");
//
//        checkPassword = "12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        assertTrue(result > 0);
//    }

    @Test
    public void testSearchUserByTags() {
        List<String> tagNameList = Arrays.asList("1234a");
        List<User> userList = userService.searchUsersByTage(tagNameList);
        Assertions.assertNotNull(userList);
    }
}