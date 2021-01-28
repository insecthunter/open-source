package com.simba.elasticjob.internal.listener;

import com.simba.elasticjob.internal.config.RescheduleListenerManager;
import com.simba.elasticjob.internal.election.ElectionListenerManager;
import com.simba.elasticjob.internal.failover.FailoverListenerManager;
import com.simba.elasticjob.internal.guarantee.GuaranteeListenerManager;
import com.simba.elasticjob.internal.instance.ShutdownListenerManager;
import com.simba.elasticjob.internal.instance.TriggerListenerManager;
import com.simba.elasticjob.internal.sharding.MonitorExecutionListenerManager;
import com.simba.elasticjob.internal.sharding.ShardingListenerManager;
import com.simba.elasticjob.internal.storage.JobNodeStorage;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

import java.util.Collection;

/**
 * @Description 总的监听器管理门面类，用于启动所有的监听器管理类
 * @Author yuanjx3
 * @Date 2021/1/19 10:00
 * @Version V1.0
 **/
public final class ListenerManager {
    private final JobNodeStorage jobNodeStorage;
    private final ElectionListenerManager electionListenerManager;
    private final ShardingListenerManager shardingListenerManager;
    private final FailoverListenerManager failoverListenerManager;
    private final MonitorExecutionListenerManager monitorExecutionListenerManager;
    private final ShutdownListenerManager shutdownListenerManager;
    private final TriggerListenerManager triggerListenerManager;
    private final RescheduleListenerManager rescheduleListenerManager;
    private final GuaranteeListenerManager guaranteeListenerManager;
    private final RegistryCenterConnectionStateListener registryCenterConnectionStateListener;

    public ListenerManager(CoordinatorRegistryCenter registryCenter, String jobName, Collection<ElasticJobListener> elasticJobListeners){
        jobNodeStorage = new JobNodeStorage(registryCenter, jobName);
        electionListenerManager = new ElectionListenerManager(registryCenter, jobName);
        shardingListenerManager = new ShardingListenerManager(registryCenter, jobName);
        failoverListenerManager = new FailoverListenerManager(registryCenter, jobName);
        monitorExecutionListenerManager = new MonitorExecutionListenerManager(registryCenter, jobName);
        shutdownListenerManager = new ShutdownListenerManager(registryCenter, jobName);
        triggerListenerManager = new TriggerListenerManager(registryCenter, jobName);
        rescheduleListenerManager = new RescheduleListenerManager(registryCenter, jobName);
        guaranteeListenerManager = new GuaranteeListenerManager(registryCenter, jobName, elasticJobListeners);
        registryCenterConnectionStateListener = new RegistryCenterConnectionStateListener(registryCenter,jobName);
    }

    public void startAllListeners(){
        electionListenerManager.start();
        shardingListenerManager.start();
        failoverListenerManager.start();
        monitorExecutionListenerManager.start();
        shutdownListenerManager.start();
        triggerListenerManager.start();
        rescheduleListenerManager.start();
        guaranteeListenerManager.start();
        jobNodeStorage.addConnectionStateListener(registryCenterConnectionStateListener);
    }
}
