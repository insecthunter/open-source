package com.simba.elasticjob.utils;

/**
 * @Description 线程阻塞工具类
 * @Author yuanjx3
 * @Date 2021/1/15 20:35
 * @Version V1.0
 **/
public class BlockUtils {
    public static void waitingShorTime(){
        sleep(100L);
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
