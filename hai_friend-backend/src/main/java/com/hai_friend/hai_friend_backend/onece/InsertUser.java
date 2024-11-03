package com.hai_friend.hai_friend_backend.onece;

import com.hai_friend.hai_friend_backend.mapper.UserMapper;
import com.hai_friend.hai_friend_backend.model.domain.User;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
public class InsertUser {

    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入用户信息
     */
//    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSEERT_NUM = 1000;
        for (int i = 0; i < INSEERT_NUM; i++) {
            User user = new User();
            user.setUsername("假嗨呀");
            user.setUserAccount("fakehaiy");
            user.setAvatarUrl("http://haidati.l-hy.site/picture/3869b3f38f4323784f19bf8f7346643420bc22234f83-EzQ5H6.png");
            user.setGender(0);
            user.setUserPassword("0243452189712627b1c4e5609986c756");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setTags("[]");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("1111");

            userMapper.insert(user);

        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
