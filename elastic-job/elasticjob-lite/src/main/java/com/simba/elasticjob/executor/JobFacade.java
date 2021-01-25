package com.simba.elasticjob.executor;

import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.event.JobExecutionEvent;
import com.simba.elasticjob.exception.JobExecutionEnvironmentException;
import com.simba.elasticjob.internal.listener.ShardingContexts;
import com.simba.elasticjob.event.JobStatusTraceEvent.State;
import java.util.Collection;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/21 15:47
 * @Version V1.0
 **/
public interface JobFacade {
    /**
     * Load job configuration.
     *
     * @param fromCache load from cache or not
     * @return job configuration
     */
    JobConfiguration loadJobConfiguration(boolean fromCache);

    /**
     * Check job execution environment.
     *
     * @throws JobExecutionEnvironmentException job execution environment exception
     */
    void checkJobExecutionEnvironment() throws JobExecutionEnvironmentException;

    /**
     * Failover If necessary.
     */
    void failoverIfNecessary();

    /**
     * Register job begin.
     *
     * @param shardingContexts sharding contexts
     */
    void registerJobBegin(ShardingContexts shardingContexts);

    /**
     * Register job completed.
     *
     * @param shardingContexts sharding contexts
     */
    void registerJobCompleted(ShardingContexts shardingContexts);

    /**
     * Get sharding contexts.
     *
     * @return sharding contexts
     */
    ShardingContexts getShardingContexts();

    /**
     * Set task misfire flag.
     *
     * @param shardingItems sharding items to be set misfire flag
     * @return whether satisfy misfire condition
     */
    boolean misfireIfRunning(Collection<Integer> shardingItems);

    /**
     * Clear misfire flag.
     *
     * @param shardingItems sharding items to be cleared misfire flag
     */
    void clearMisfire(Collection<Integer> shardingItems);

    /**
     * Judge job whether need to execute misfire tasks.
     *
     * @param shardingItems sharding items
     * @return whether need to execute misfire tasks
     */
    boolean isExecuteMisfired(Collection<Integer> shardingItems);

    /**
     * Judge job whether need resharding.
     *
     * @return whether need resharding
     */
    boolean isNeedSharding();

    /**
     * Call before job executed.
     *
     * @param shardingContexts sharding contexts
     */
    void beforeJobExecuted(ShardingContexts shardingContexts);

    /**
     * Call after job executed.
     *
     * @param shardingContexts sharding contexts
     */
    void afterJobExecuted(ShardingContexts shardingContexts);

    /**
     * Post job execution event.
     *
     * @param jobExecutionEvent job execution event
     */
    void postJobExecutionEvent(JobExecutionEvent jobExecutionEvent);

    /**
     * Post job status trace event.
     *
     * @param taskId task Id
     * @param state job state
     * @param message job message
     */
    void postJobStatusTraceEvent(String taskId, State state, String message);
}
