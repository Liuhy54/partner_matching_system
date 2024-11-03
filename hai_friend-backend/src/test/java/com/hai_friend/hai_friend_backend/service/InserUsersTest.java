package com.hai_friend.hai_friend_backend.service;

import com.hai_friend.hai_friend_backend.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class InserUsersTest {

    @Resource
    private UserService userService;

    //线程池
    //上限因素：
    // CPU密集型：CPU核数越多，线程数越多，效率越高 分配核心线程数 = CPU -1
    // IO密集型：分配线程核心数可以大于 CPU 核数，因为 IO 等待时间长，可以多分配一些线程
    // 内存密集型：线程数应设为CPU核数的1-4倍，因为线程数过多会导致内存消耗过多，降低效率
    private ExecutorService executorService = new ThreadPoolExecutor(60,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));

    /**
     * 批量插入用户信息
     */
    @Test
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>(INSERT_NUM);
        for (int i = 0; i < INSERT_NUM; i++) {
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
            userList.add(user);

        }
        userService.saveBatch(userList,10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 并发批量插入用户信息
     */
    @Test
    public void doConcurrentInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        // 并发数 10
        int batchSize = 2500;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            List<User> userList = new ArrayList<>(INSERT_NUM);
            while (true){
                j++;
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
                userList.add(user);
                if(j % batchSize == 0){
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("线程：" + Thread.currentThread().getName() + "开始执行");
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
