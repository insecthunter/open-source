package com.simba.elasticjob.event;

import java.util.Date;
import java.util.UUID;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/21 15:53
 * @Version V1.0
 **/
public final class JobExecutionEvent implements JobEvent {
    private String id = UUID.randomUUID().toString();
    private final String hostname;
    private final String ip;
    private final String taskId;
    private final String jobName;
    private final ExecutionSource source;
    private final int shardingItem;
    private Date startTime = new Date();

    private Date completeTime;
    private boolean success;
    private String failureCause;

    /** Required Args Constructor **/
    public JobExecutionEvent(String hostname, String ip, String taskId, String jobName, ExecutionSource source, int shardingItem) {
        this.hostname = hostname;
        this.ip = ip;
        this.taskId = taskId;
        this.jobName = jobName;
        this.source = source;
        this.shardingItem = shardingItem;
    }

    /** All Args Constructor **/
    public JobExecutionEvent(String id, String hostname, String ip, String taskId, String jobName, ExecutionSource source, int shardingItem, Date startTime, Date completeTime, boolean success, String failureCause) {
        this.id = id;
        this.hostname = hostname;
        this.ip = ip;
        this.taskId = taskId;
        this.jobName = jobName;
        this.source = source;
        this.shardingItem = shardingItem;
        this.startTime = startTime;
        this.completeTime = completeTime;
        this.success = success;
        this.failureCause = failureCause;
    }

    /**
     * Execution success.
     *
     * @return job execution event
     */
    public JobExecutionEvent executionSuccess() {
        JobExecutionEvent result = new JobExecutionEvent(id, hostname, ip, taskId, jobName, source, shardingItem, startTime, completeTime, success, failureCause);
        result.setCompleteTime(new Date());
        result.setSuccess(true);
        return result;
    }

    /**
     * Execution failure.
     *
     * @param failureCause failure cause
     * @return job execution event
     */
    public JobExecutionEvent executionFailure(final String failureCause) {
        JobExecutionEvent result = new JobExecutionEvent(id, hostname, ip, taskId, jobName, source, shardingItem, startTime, completeTime, success, failureCause);
        result.setCompleteTime(new Date());
        result.setSuccess(false);
        result.setFailureCause(failureCause);
        return result;
    }

    /**
     * Execution source.
     */
    public enum ExecutionSource {
        NORMAL_TRIGGER, MISFIRE, FAILOVER
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setFailureCause(String failureCause) {
        this.failureCause = failureCause;
    }

    public String getId() {
        return id;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public String getTaskId() {
        return taskId;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    public ExecutionSource getSource() {
        return source;
    }

    public int getShardingItem() {
        return shardingItem;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFailureCause() {
        return failureCause;
    }

    @Override
    public String toString() {
        return "JobExecutionEvent{" +
                "id='" + id + '\'' +
                ", hostname='" + hostname + '\'' +
                ", ip='" + ip + '\'' +
                ", taskId='" + taskId + '\'' +
                ", jobName='" + jobName + '\'' +
                ", source=" + source +
                ", shardingItem=" + shardingItem +
                ", startTime=" + startTime +
                ", completeTime=" + completeTime +
                ", success=" + success +
                ", failureCause='" + failureCause + '\'' +
                '}';
    }
}
