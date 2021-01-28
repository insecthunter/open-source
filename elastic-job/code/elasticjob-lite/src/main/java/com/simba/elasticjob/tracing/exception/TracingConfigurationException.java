package com.simba.elasticjob.tracing.exception;

/**
 * @Description Tracing Configuration Exception
 * @Author yuanjx3
 * @Date 2021/1/21 19:38
 * @Version V1.0
 **/
public class TracingConfigurationException extends Exception {

    private static final long serialVersionUID = 4069519372148227761L;

    public TracingConfigurationException(final Exception ex) {
        super(ex);
    }

    public TracingConfigurationException(final String errorMessage) {
        super(errorMessage);
    }
}
