package com.simba.elasticjob.api;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/17 16:35
 * @Version V1.0
 **/
public final class ShardingContext {
    private final String jobName;

    private final String taskId;

    private final int shardingTotalCount;

    private final String jobParameter;

    private final int shardingItem;

    private final String shardingParameter;

    public ShardingContext(String jobName, String taskId, int shardingTotalCount, String jobParameter, int shardingItem, String shardingParameter) {
        this.jobName = jobName;
        this.taskId = taskId;
        this.shardingTotalCount = shardingTotalCount;
        this.jobParameter = jobParameter;
        this.shardingItem = shardingItem;
        this.shardingParameter = shardingParameter;
    }

    public String getJobName() {
        return jobName;
    }

    public String getTaskId() {
        return taskId;
    }

    public int getShardingTotalCount() {
        return shardingTotalCount;
    }

    public String getJobParameter() {
        return jobParameter;
    }

    public int getShardingItem() {
        return shardingItem;
    }

    public String getShardingParameter() {
        return shardingParameter;
    }

    @Override
    public String toString() {
        return "ShardingContext{" +
                "jobName='" + jobName + '\'' +
                ", taskId='" + taskId + '\'' +
                ", shardingTotalCount=" + shardingTotalCount +
                ", jobParameter='" + jobParameter + '\'' +
                ", shardingItem=" + shardingItem +
                ", shardingParameter='" + shardingParameter + '\'' +
                '}';
    }
}
