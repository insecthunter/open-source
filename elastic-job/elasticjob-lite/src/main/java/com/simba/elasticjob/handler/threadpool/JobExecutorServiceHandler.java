package com.simba.elasticjob.handler.threadpool;


import com.simba.elasticjob.spi.TypedSPI;

import java.util.concurrent.ExecutorService;

/**
 * Job executor service handler.
 */
public interface JobExecutorServiceHandler extends TypedSPI {
    
    /**
     * Create executor service.
     * 
     * @param jobName job name
     * 
     * @return executor service
     */
    ExecutorService createExecutorService(String jobName);
}
