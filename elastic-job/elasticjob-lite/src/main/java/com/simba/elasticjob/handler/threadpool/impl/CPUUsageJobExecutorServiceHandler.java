package com.simba.elasticjob.handler.threadpool.impl;

/**
 * Job executor service handler with use CPU available processors.
 */
public final class CPUUsageJobExecutorServiceHandler extends AbstractJobExecutorServiceHandler {
    
    @Override
    protected int getPoolSize() {
        return Runtime.getRuntime().availableProcessors() * 2;
    }
    
    @Override
    public String getType() {
        return "CPU";
    }
}
