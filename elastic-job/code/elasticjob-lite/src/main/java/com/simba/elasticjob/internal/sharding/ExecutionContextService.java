package com.simba.elasticjob.internal.sharding;

import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.context.ShardingItemParameters;
import com.simba.elasticjob.handler.sharding.JobInstance;
import com.simba.elasticjob.internal.config.ConfigurationService;
import com.simba.elasticjob.internal.listener.ShardingContexts;
import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.storage.JobNodeStorage;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/21 16:17
 * @Version V1.0
 **/
public final class ExecutionContextService {
    private final String jobName;
    private final JobNodeStorage jobNodeStorage;
    private final ConfigurationService configService;

    public ExecutionContextService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.jobName = jobName;
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        configService = new ConfigurationService(regCenter, jobName);
    }

    /**
     * Get job sharding context.
     *
     * @param shardingItems sharding items
     * @return job sharding context
     */
    public ShardingContexts getJobShardingContext(final List<Integer> shardingItems) {
        JobConfiguration jobConfig = configService.load(false);
        removeRunningIfMonitorExecution(jobConfig.isMonitorExecution(), shardingItems);
        if (shardingItems.isEmpty()) {
            return new ShardingContexts(buildTaskId(jobConfig, shardingItems), jobConfig.getJobName(), jobConfig.getShardingTotalCount(),
                    jobConfig.getJobParameter(), Collections.emptyMap());
        }
        Map<Integer, String> shardingItemParameterMap = new ShardingItemParameters(jobConfig.getShardingItemParameters()).getMap();
        return new ShardingContexts(buildTaskId(jobConfig, shardingItems), jobConfig.getJobName(), jobConfig.getShardingTotalCount(),
                jobConfig.getJobParameter(), getAssignedShardingItemParameterMap(shardingItems, shardingItemParameterMap));
    }

    private String buildTaskId(final JobConfiguration jobConfig, final List<Integer> shardingItems) {
        JobInstance jobInstance = JobRegistry.getInstance().getJobInstance(jobName);
        String shardingItemsString = shardingItems.stream().map(Object::toString).collect(Collectors.joining(","));
        String jobInstanceId = null == jobInstance.getJobInstanceId() ? "127.0.0.1@-@1" : jobInstance.getJobInstanceId();
        return String.join("@-@", jobConfig.getJobName(), shardingItemsString, "READY", jobInstanceId);
    }

    private void removeRunningIfMonitorExecution(final boolean monitorExecution, final List<Integer> shardingItems) {
        if (!monitorExecution) {
            return;
        }
        List<Integer> runningShardingItems = new ArrayList<>(shardingItems.size());
        for (int each : shardingItems) {
            if (isRunning(each)) {
                runningShardingItems.add(each);
            }
        }
        shardingItems.removeAll(runningShardingItems);
    }

    private boolean isRunning(final int shardingItem) {
        return jobNodeStorage.isJobNodeExisted(ShardingNode.getRunningNode(shardingItem));
    }

    private Map<Integer, String> getAssignedShardingItemParameterMap(final List<Integer> shardingItems, final Map<Integer, String> shardingItemParameterMap) {
        Map<Integer, String> result = new HashMap<>(shardingItems.size(), 1);
        for (int each : shardingItems) {
            result.put(each, shardingItemParameterMap.get(each));
        }
        return result;
    }
}
