package com.simba.elasticjob.event;

import java.util.Date;
import java.util.UUID;

/**
 * @Description Job status trace event
 * @Author yuanjx3
 * @Date 2021/1/21 16:03
 * @Version V1.0
 **/
public final class JobStatusTraceEvent implements JobEvent {
    private String id = UUID.randomUUID().toString();
    private final String jobName;
    private String originalTaskId = "";
    private final String taskId;
    private final String slaveId;
    private final Source source;
    private final String executionType;
    private final String shardingItems;
    private final State state;
    private final String message;
    private Date creationTime = new Date();

    public JobStatusTraceEvent(String jobName, String taskId, String slaveId, Source source, String executionType, String shardingItems, State state, String message) {
        this.jobName = jobName;
        this.taskId = taskId;
        this.slaveId = slaveId;
        this.source = source;
        this.executionType = executionType;
        this.shardingItems = shardingItems;
        this.state = state;
        this.message = message;
    }

    public enum State {
        TASK_STAGING, TASK_RUNNING, TASK_FINISHED, TASK_KILLED, TASK_LOST, TASK_FAILED, TASK_ERROR, TASK_DROPPED, TASK_GONE, TASK_GONE_BY_OPERATOR, TASK_UNREACHABLE, TASK_UNKNOWN
    }

    public enum Source {
        CLOUD_SCHEDULER, CLOUD_EXECUTOR, LITE_EXECUTOR
    }
    public void setOriginalTaskId(String originalTaskId) {
        this.originalTaskId = originalTaskId;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    public String getOriginalTaskId() {
        return originalTaskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getSlaveId() {
        return slaveId;
    }

    public Source getSource() {
        return source;
    }

    public String getExecutionType() {
        return executionType;
    }

    public String getShardingItems() {
        return shardingItems;
    }

    public State getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    @Override
    public String toString() {
        return "JobStatusTraceEvent{" +
                "id='" + id + '\'' +
                ", jobName='" + jobName + '\'' +
                ", originalTaskId='" + originalTaskId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", slaveId='" + slaveId + '\'' +
                ", source=" + source +
                ", executionType='" + executionType + '\'' +
                ", shardingItems='" + shardingItems + '\'' +
                ", state=" + state +
                ", message='" + message + '\'' +
                ", creationTime=" + creationTime +
                '}';
    }
}
