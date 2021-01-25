package com.simba.elasticjob.concurrent;

import java.time.Duration;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/17 14:47
 * @Version V1.0
 **/
public final class Internal {
    static long toNanosSaturated(Duration duration) {
        try {
            //getNano() 获取纳秒数,获取的是纳秒部分的值,而不是转换成纳秒
            return duration.toNanos();
        } catch (ArithmeticException var2) {
            return duration.isNegative() ? -9223372036854775808L : 9223372036854775807L;
        }
    }

    private Internal() {
    }
}
