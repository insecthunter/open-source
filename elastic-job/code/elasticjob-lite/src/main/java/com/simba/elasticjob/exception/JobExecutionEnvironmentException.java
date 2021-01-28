package com.simba.elasticjob.exception;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/15 15:37
 * @Version V1.0
 **/
public class JobExecutionEnvironmentException extends Exception {
    public JobExecutionEnvironmentException(String errorMessage, Object... args){
        super(String.format(errorMessage, args));
    }
}
