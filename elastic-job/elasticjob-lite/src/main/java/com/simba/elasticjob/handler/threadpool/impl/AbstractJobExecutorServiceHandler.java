package com.simba.elasticjob.handler.threadpool.impl;

import com.simba.elasticjob.concurrent.ElasticJobExecutorService;
import com.simba.elasticjob.handler.threadpool.JobExecutorServiceHandler;

import java.util.concurrent.ExecutorService;

/**
 * Abstract job executor service handler.
 **/
public abstract class AbstractJobExecutorServiceHandler implements JobExecutorServiceHandler {
    
    @Override
    public ExecutorService createExecutorService(final String jobName) {
        // 创建线程池，执行作业
        return new ElasticJobExecutorService("elasticjob-" + jobName, getPoolSize()).createExecutorService();
    }
    
    protected abstract int getPoolSize();
}
