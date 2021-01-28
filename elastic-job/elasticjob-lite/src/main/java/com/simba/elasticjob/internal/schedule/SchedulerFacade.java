package com.simba.elasticjob.internal.schedule;

import com.simba.elasticjob.internal.election.LeaderService;
import com.simba.elasticjob.internal.reconcile.ReconcileService;
import com.simba.elasticjob.internal.sharding.ExecutionService;
import com.simba.elasticjob.internal.sharding.ShardingService;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

/**
 * @Description 调度管理器（Scheduler facade）
 * @Author yuanjx3
 * @Date 2021/1/19 15:47
 * @Version V1.0
 **/
public final class SchedulerFacade {
    private final String jobName;
    private final LeaderService leaderService;
    private final ShardingService shardingService;
    private final ExecutionService executionService;
    private final ReconcileService reconcileService;

    public SchedulerFacade(CoordinatorRegistryCenter registryCenter, String jobName){
        this.jobName = jobName;
        leaderService = new LeaderService(registryCenter, jobName);
        shardingService = new ShardingService(registryCenter, jobName);
        executionService = new ExecutionService(registryCenter, jobName);
        reconcileService = new ReconcileService(registryCenter, jobName);
    }

    /**
     * Create job trigger listener.
     * @return job trigger listener
     */
    public JobTriggerListener newJobTriggerListener(){
        return new JobTriggerListener(executionService, shardingService);
    }

    /** 停机服务器实例 **/
    public void shutdownInstance() {
        //如果当前节点是leader，则移除Leader
        if (leaderService.isLeader()){
            leaderService.removeLeader();
        }

        // 如果协调服务正在运行，先暂停
        if (reconcileService.isRunning()){
            reconcileService.stopAsync();
        }
        // 执行停机
        JobRegistry.getInstance().shutdown(jobName);
    }
}
