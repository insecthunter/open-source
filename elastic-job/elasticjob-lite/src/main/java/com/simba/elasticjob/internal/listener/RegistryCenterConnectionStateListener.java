package com.simba.elasticjob.internal.listener;

import com.simba.elasticjob.internal.instance.InstanceService;
import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.schedule.JobScheduleController;
import com.simba.elasticjob.internal.server.ServerService;
import com.simba.elasticjob.internal.sharding.ExecutionService;
import com.simba.elasticjob.internal.sharding.ShardingService;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * @Description 注册中心连接状态监听器
 * @Author yuanjx3
 * @Date 2021/1/21 12:10
 * @Version V1.0
 **/
public final class RegistryCenterConnectionStateListener implements ConnectionStateListener {
    private final String jobName;
    private final ServerService serverService;
    private final InstanceService instanceService;
    private final ShardingService shardingService;
    private final ExecutionService executionService;

    public RegistryCenterConnectionStateListener(CoordinatorRegistryCenter registryCenter, String jobName){
        this.jobName = jobName;
        serverService = new ServerService(registryCenter, jobName);
        instanceService = new InstanceService(registryCenter, jobName);
        shardingService = new ShardingService(registryCenter, jobName);
        executionService = new ExecutionService(registryCenter, jobName);
    }
    @Override
    public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
        if (JobRegistry.getInstance().isShutdown(jobName)){
            return;
        }
        JobScheduleController jobScheduleController = JobRegistry.getInstance().getJobScheduleController(jobName);
        // 如果注册中心连接中断或者被挂起，则通过任务调度控制器暂停所有任务调度执行
        if (ConnectionState.SUSPENDED == connectionState || ConnectionState.LOST == connectionState){
            jobScheduleController.pauseJob();
        }else if (ConnectionState.RECONNECTED == connectionState){
            // 如果注册中心连接恢复，则重启任务调度
            serverService.persistOnline(serverService.isEnableServer(JobRegistry.getInstance().getJobInstance(jobName).getIp()));
            instanceService.persistOnline();
            executionService.clearRunningInfo(shardingService.getLocalShardingItems());
            jobScheduleController.resumeJob();
        }
    }
}
