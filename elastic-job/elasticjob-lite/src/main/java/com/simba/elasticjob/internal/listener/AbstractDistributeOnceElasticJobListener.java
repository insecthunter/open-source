package com.simba.elasticjob.internal.listener;

import com.simba.elasticjob.exception.JobSystemException;
import com.simba.elasticjob.internal.config.TimeService;
import com.simba.elasticjob.internal.guarantee.GuaranteeService;
import com.simba.elasticjob.utils.BlockUtils;

import java.util.Set;

/**
 * @Description 分布式一次性elasticjob监听器
 * @Author yuanjx3
 * @Date 2021/1/19 17:39
 * @Version V1.0
 **/
public abstract class AbstractDistributeOnceElasticJobListener implements ElasticJobListener{
    private final long startedTimeoutMilliseconds;
    private final Object startedWait = new Object();
    private final long completedTimeoutMilliseconds;
    private final Object completedWait = new Object();
    private GuaranteeService guaranteeService;
    private final TimeService timeService = new TimeService();

    public AbstractDistributeOnceElasticJobListener(final long startedTimeoutMilliseconds, final long completedTimeoutMilliseconds) {
        this.startedTimeoutMilliseconds = startedTimeoutMilliseconds <= 0L ? Long.MAX_VALUE : startedTimeoutMilliseconds;
        this.completedTimeoutMilliseconds = completedTimeoutMilliseconds <= 0L ? Long.MAX_VALUE : completedTimeoutMilliseconds;
    }

    public void setGuaranteeService(GuaranteeService guaranteeService) {
        this.guaranteeService = guaranteeService;
    }

    @Override
    public void beforeJobExecuted(ShardingContexts shardingContexts) {
        Set<Integer> shardingItems = shardingContexts.getShardingItemParameters().keySet();
        if (shardingItems.isEmpty()){
            return;
        }
        guaranteeService.registerStart(shardingItems);
        while (!guaranteeService.isRegisterStartSuccess(shardingItems)){
            BlockUtils.waitingShorTime();
        }
        if (guaranteeService.isAllStarted()){
            doBeforeJobExecutedAtLastStarted(shardingContexts);
            guaranteeService.clearAllStartedInfo();
            return;
        }
        long before = timeService.getCurrentMillis();
        try {
            synchronized (startedWait) {
                startedWait.wait(startedTimeoutMilliseconds);
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        if (timeService.getCurrentMillis() - before >= startedTimeoutMilliseconds) {
            guaranteeService.clearAllStartedInfo();
            handleTimeout(startedTimeoutMilliseconds);
        }
    }

    @Override
    public void afterJobExecuted(ShardingContexts shardingContexts) {
        Set<Integer> shardingItems = shardingContexts.getShardingItemParameters().keySet();
        if (shardingItems.isEmpty()){
            return;
        }
        guaranteeService.registerComplete(shardingItems);
        while (!guaranteeService.isRegisterCompleteSuccess(shardingItems)) {
            BlockUtils.waitingShorTime();
        }
        if (guaranteeService.isAllCompleted()) {
            doAfterJobExecutedAtLastCompleted(shardingContexts);
            guaranteeService.clearAllCompletedInfo();
            return;
        }
        long before = timeService.getCurrentMillis();
        try {
            synchronized (completedWait){
                completedWait.wait(completedTimeoutMilliseconds);
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        if (timeService.getCurrentMillis() - before >= completedTimeoutMilliseconds) {
            guaranteeService.clearAllCompletedInfo();
            handleTimeout(completedTimeoutMilliseconds);
        }
    }

    private void handleTimeout(long timeoutMilliseconds) {
        throw new JobSystemException("作业超时。超时时间为 %s", timeoutMilliseconds);
    }

    /**
     * Do after job executed at last sharding job completed.
     * @param shardingContexts sharding contexts
     */
    public abstract void doBeforeJobExecutedAtLastStarted(ShardingContexts shardingContexts);

    /**
     * Do after job executed at last sharding job completed.
     * @param shardingContexts sharding contexts
     */
    public abstract void doAfterJobExecutedAtLastCompleted(ShardingContexts shardingContexts);

    /** 通知正在等待的任务启动 **/
    public void notifyWaitingTaskStart() {
        synchronized (startedWait) {
            startedWait.notifyAll();
        }
    }

    /** 通知正在等待的任务完成 **/
    public void notifyWaitingTaskComplete() {
        synchronized (completedWait) {
            completedWait.notifyAll();
        }
    }
}
