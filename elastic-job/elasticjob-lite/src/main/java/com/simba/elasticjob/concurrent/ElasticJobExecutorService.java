package com.simba.elasticjob.concurrent;

import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.*;

/**
 * @Description ElasticJob Executor Service
 * @Author yuanjx3
 * @Date 2021/1/22 9:00
 * @Version V1.0
 **/
public class ElasticJobExecutorService {
    private final ThreadPoolExecutor threadPoolExecutor;

    private final BlockingQueue<Runnable> workQueue;

    public ElasticJobExecutorService(final String namingPattern, final int threadSize) {
        workQueue = new LinkedBlockingQueue<>();
        threadPoolExecutor = new ThreadPoolExecutor(
                threadSize, threadSize, 5L, TimeUnit.MINUTES, workQueue, new BasicThreadFactory.Builder().namingPattern(String.join("-", namingPattern, "%s")).build());
        threadPoolExecutor.allowCoreThreadTimeOut(true);
    }

    /**
     * Create executor service.
     *
     * @return executor service
     */
    public ExecutorService createExecutorService() {
        return MoreExecutors.listeningDecorator(MoreExecutors.getExitingExecutorService(threadPoolExecutor));
    }

    /**
     * Whether the threadPoolExecutor has been shut down.
     *
     * @return Whether the threadPoolExecutor has been shut down
     */
    public boolean isShutdown() {
        return threadPoolExecutor.isShutdown();
    }

    /**
     * Get active thread count.
     *
     * @return active thread count
     */
    public int getActiveThreadCount() {
        return threadPoolExecutor.getActiveCount();
    }

    /**
     * Get work queue size.
     *
     * @return work queue size
     */
    public int getWorkQueueSize() {
        return workQueue.size();
    }
}
