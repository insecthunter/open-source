package com.simba.elasticjob.internal.listener;

import com.simba.elasticjob.api.ShardingContext;

import java.io.Serializable;
import java.util.Map;

/**
 * @Description 分片上下文
 * @Author yuanjx3
 * @Date 2021/1/19 17:21
 * @Version V1.0
 **/
public final class ShardingContexts implements Serializable {
    private final String taskId;
    private final String jobName;
    private final int shardingTotalCount;
    private final String jobParameter;
    private final Map<Integer, String> shardingItemParameters;
    private int jobEventSamplingCount;

    private int currentJobEventSamplingCount;
    private boolean allowSendJobEvent = true;

    public ShardingContexts(String taskId, String jobName, int shardingTotalCount, String jobParameter, Map<Integer, String> shardingItemParameters) {
        this.taskId = taskId;
        this.jobName = jobName;
        this.shardingTotalCount = shardingTotalCount;
        this.jobParameter = jobParameter;
        this.shardingItemParameters = shardingItemParameters;
    }

    public ShardingContext createShardingContext(int shardingItem) {
        return new ShardingContext(jobName,taskId,shardingTotalCount,jobParameter,shardingItem,shardingItemParameters.get(shardingItem));
    }

    public void setCurrentJobEventSamplingCount(int currentJobEventSamplingCount) {
        this.currentJobEventSamplingCount = currentJobEventSamplingCount;
    }

    public void setAllowSendJobEvent(boolean allowSendJobEvent) {
        this.allowSendJobEvent = allowSendJobEvent;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getJobName() {
        return jobName;
    }

    public int getShardingTotalCount() {
        return shardingTotalCount;
    }

    public String getJobParameter() {
        return jobParameter;
    }

    public Map<Integer, String> getShardingItemParameters() {
        return shardingItemParameters;
    }

    public int getJobEventSamplingCount() {
        return jobEventSamplingCount;
    }

    public int getCurrentJobEventSamplingCount() {
        return currentJobEventSamplingCount;
    }

    public boolean isAllowSendJobEvent() {
        return allowSendJobEvent;
    }
}
