package com.simba.elasticjob.handler.threadpool.impl;

import com.simba.elasticjob.concurrent.ElasticJobExecutorService;
import com.simba.elasticjob.handler.threadpool.JobExecutorServiceHandler;

import java.util.concurrent.ExecutorService;

/**
 * Abstract job executor service handler.
 * 抽象作业执行服务处理器，用于创建作业执行服务
 **/
public abstract class AbstractJobExecutorServiceHandler implements JobExecutorServiceHandler {
    
    @Override
    public ExecutorService createExecutorService(final String jobName) {
        // 创建作业执行服务
        return new ElasticJobExecutorService("elasticjob-" + jobName, getPoolSize()).createExecutorService();
    }
    
    protected abstract int getPoolSize();
}
