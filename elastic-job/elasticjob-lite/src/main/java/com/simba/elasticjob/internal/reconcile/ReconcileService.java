package com.simba.elasticjob.internal.reconcile;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.simba.elasticjob.internal.config.ConfigurationService;
import com.simba.elasticjob.internal.sharding.ShardingService;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

import java.util.concurrent.TimeUnit;

/**
 * @Description 协调服务 （这里的并发处理使用了Google的 guava 框架）
 * @Author yuanjx3
 * @Date 2021/1/17 14:44
 * @Version V1.0
 **/
public class ReconcileService extends AbstractScheduledService {
    private long lastReconcileTime;
    private ConfigurationService configurationService;
    private ShardingService shardingService;

    public ReconcileService(CoordinatorRegistryCenter registryCenter, String jobName){
        lastReconcileTime = System.currentTimeMillis();
        configurationService = new ConfigurationService(registryCenter, jobName);
        shardingService = new ShardingService(registryCenter, jobName);
    }

    @Override
    protected void runOneIteration() throws Exception {
        int reconcileIntervalMinutes = configurationService.load(true).getReconcileIntervalMinutes();
        if ( reconcileIntervalMinutes > 0
                && (System.currentTimeMillis() - lastReconcileTime >= reconcileIntervalMinutes*60*1000)){
            lastReconcileTime = System.currentTimeMillis();
            if (!shardingService.isNeedSharding() && shardingService.hasShardingInfoInOfflineServers()) {
                System.out.println("Elastic Job:  job status node has inconsistent value, start reconciling【作业状态节点的值不一致，开始重新协调。。。。】...");
                shardingService.setReshardingFlag();
            }
        }
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0,1, TimeUnit.MINUTES);
    }
}
