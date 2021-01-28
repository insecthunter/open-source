package com.simba.elasticjob.exception;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/15 15:14
 * @Version V1.0
 **/
public class JobConfigurationException extends RuntimeException {
    public JobConfigurationException(final String errorMessage, final Object... args) {
        super(String.format(errorMessage, args));
    }

    public JobConfigurationException(final Throwable cause) {
        super(cause);
    }
}
