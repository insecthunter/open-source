package com.simba.elasticjob.exception;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/15 9:20
 * @Version V1.0
 **/
public class JobSystemException extends RuntimeException {
    public JobSystemException(final String errorMessage, final Object... args) {
        super(String.format(errorMessage, args));
    }

    public JobSystemException(final Throwable cause) {
        super(cause);
    }
}
