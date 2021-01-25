package com.simba.elasticjob.exception;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/12 15:21
 * @Version V1.0
 **/
public class RegException extends RuntimeException{

    private static final long serialVersionUID = -6417179023552012152L;

    public RegException(final Exception cause) {
        super(cause);
    }
}
