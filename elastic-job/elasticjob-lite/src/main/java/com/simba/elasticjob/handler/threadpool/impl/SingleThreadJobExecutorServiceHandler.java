package com.simba.elasticjob.handler.threadpool.impl;

/**
 * Job executor service handler with single thread.
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
