package com.simba.elasticjob.utils;


/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/17 15:05
 * @Version V1.0
 **/
public class Preconditions {
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        } else {
            return reference;
        }
    }

    public static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static void checkArgument(boolean b, String errorMessageTemplate, long p1) {
        if (!b) {
            throw new IllegalArgumentException(StringUtils.lenientFormat(errorMessageTemplate, new Object[]{p1}));
        }
    }
}
