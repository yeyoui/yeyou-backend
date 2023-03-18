package com.yeyou.yeyoubackend.service;

import com.yeyou.yeyoubackend.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class AlgorithmTest {
    @Test
    public void testDistanceTag(){
        List<String> list1 = Arrays.asList("java","蓝桥","ikun","C++","nihao","Steam");
        List<String> list2 = Arrays.asList("java","12","ikun","C++","nihao","Steam");
        List<String> list3 = Arrays.asList("Java");

        System.out.println(AlgorithmUtils.minDistance(list1,list2));
    }
}
