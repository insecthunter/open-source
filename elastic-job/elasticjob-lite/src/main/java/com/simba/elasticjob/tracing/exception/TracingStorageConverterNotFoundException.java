package com.simba.elasticjob.tracing.exception;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/21 19:31
 * @Version V1.0
 **/
public class TracingStorageConverterNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -995858641205565452L;

    public TracingStorageConverterNotFoundException(final Class<?> storageType) {
        super(String.format("No TracingConfigurationConverter found for [%s]", storageType.getName()));
    }
}
