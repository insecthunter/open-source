package com.simba.elasticjob.spi.exception;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/18 17:37
 * @Version V1.0
 **/
public class ServiceLoaderInstantiationException extends RuntimeException {
    public  ServiceLoaderInstantiationException(Class<?> clazz, Throwable cause){
        super(String.format("Can not find public no args constructor for SPI class `%s`", clazz.getName()), cause);
    }
}
