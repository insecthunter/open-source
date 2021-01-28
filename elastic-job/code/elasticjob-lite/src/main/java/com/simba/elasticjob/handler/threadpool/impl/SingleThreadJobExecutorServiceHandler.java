package com.simba.elasticjob.handler.threadpool.impl;

/**
 * Job executor service handler with single thread.
 * 单线程作业执行服务处理器
 */
public final class SingleThreadJobExecutorServiceHandler extends AbstractJobExecutorServiceHandler {
    
    @Override
    protected int getPoolSize() {
        return 1;
    }
    
    @Override
    public String getType() {
        return "SINGLE_THREAD";
    }
}
