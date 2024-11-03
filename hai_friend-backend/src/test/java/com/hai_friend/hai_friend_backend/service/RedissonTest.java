package com.hai_friend.hai_friend_backend.service;

import com.hai_friend.hai_friend_backend.config.RedissonConfig;
import com.hai_friend.hai_friend_backend.contant.redissonConstant;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonConfig redissonConfig;
    @Autowired
    private RedissonClient redissonClient;

    @Test
    void test(){
        //list 列表, 数据存在本地 JVM 内存中
        List<String>  list = new ArrayList<>();
        list.add("haiy");
        String testList = list.get(0);
        System.out.println("List: " + testList);
        list.remove(0);

        //redisson 列表, 数据存在 redis 数据库中
        RList<String> rList = redissonClient.getList("test-list");
        rList.add("haiypig");
        testList = rList.get(0);
        System.out.println("redisson List: " + testList);
        rList.remove(0);

        // map 字典, 数据存在本地 JVM 内存中
        Map<String, Integer> map = new HashMap<>();
        map.put("haiy", 1);
        map.get("haiy");

        //redisson 字典, 数据存在 redis 数据库中
        RMap<String, Integer> rMap = redissonClient.getMap("test-map");
        rMap.put("haiypig", 2);
    }

    @Test
    void testWaichDog(){
        RLock lock = redissonClient.getLock(redissonConstant.REDIS_CHANNEL_PRECACHEJOB_CACHE_LOCK);
        try {
            // 只有一个线程可以执行，其他线程等待
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                Thread.sleep(1000000);
                System.out.println("getLock:" + Thread.currentThread().getId());

            }
        } catch (InterruptedException e) {
            System.out.println("InterruptedException:" + e.getMessage());
        } finally {
            //只能释放自己的锁
            if (lock.isHeldByCurrentThread()){
                System.out.println("unLock:" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
