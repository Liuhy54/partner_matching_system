package com.hai_friend.hai_friend_backend.service;

import com.hai_friend.hai_friend_backend.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class AlgorihmUtilsTest {
    @Test
    void test() {
        String str1 = "鱼皮是狗";
        String str2 = "鱼皮不是狗";
        String str3 = "鱼皮是鱼不是狗";
        String str4 = "鱼皮是猫";
        // 1
        int score1 = AlgorithmUtils.minDistance(str1, str2);
        // 3
        int score2 = AlgorithmUtils.minDistance(str1, str3);
        System.out.println(score1);
        System.out.println(score2);
    }

    @Test
    void testCompareTags() {
        List<String> taglist1 = Arrays.asList("Java", "大一", "男");
        List<String> taglist2 = Arrays.asList("Java", "大二", "男");
        List<String> taglist3 = Arrays.asList("Python", "大三", "女");
        // 1
        int score1 = AlgorithmUtils.minDistance(taglist1, taglist2);
        // 3
        int score2 = AlgorithmUtils.minDistance(taglist1, taglist3);
        System.out.println(score1);
        System.out.println(score2);
    }

}
