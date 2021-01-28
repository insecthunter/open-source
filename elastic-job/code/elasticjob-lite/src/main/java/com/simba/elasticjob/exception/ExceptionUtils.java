package com.simba.elasticjob.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/22 9:13
 * @Version V1.0
 **/
public final class ExceptionUtils {
    private ExceptionUtils(){}
    /**
     * Transform throwable to string.
     *
     * @param cause cause
     * @return string
     */
    public static String transform(final Throwable cause) {
        if (null == cause) {
            return "";
        }
        StringWriter result = new StringWriter();
        try (PrintWriter writer = new PrintWriter(result)) {
            cause.printStackTrace(writer);
        }
        return result.toString();
    }
}
