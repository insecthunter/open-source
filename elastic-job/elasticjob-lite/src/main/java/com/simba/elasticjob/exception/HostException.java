package com.simba.elasticjob.exception;

import java.io.IOException;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/15 16:03
 * @Version V1.0
 **/
public class HostException extends RuntimeException {
    public HostException(final IOException cause) {
        super(cause);
    }

    public HostException(final String message) {
        super(message);
    }
}
