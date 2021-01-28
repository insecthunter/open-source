package com.simba.elasticjob.internal.schedule;

import com.simba.elasticjob.internal.sharding.ExecutionService;
import com.simba.elasticjob.internal.sharding.ShardingService;
import org.quartz.Trigger;
import org.quartz.listeners.TriggerListenerSupport;

/**
 * @Description 作业触发监听器（Job trigger listener.）
 * @Author yuanjx3
 * @Date 2021/1/19 15:56
 * @Version V1.0
 **/
public final class JobTriggerListener extends TriggerListenerSupport {
    private final ExecutionService executionService;
    private final ShardingService shardingService;

    public JobTriggerListener(ExecutionService executionService, ShardingService shardingService) {
        this.executionService = executionService;
        this.shardingService = shardingService;
    }

    @Override
    public String getName() {
        return "JobTriggerListener";
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        if (null != trigger.getPreviousFireTime()){
            executionService.setMisfire(shardingService.getLocalShardingItems());
        }
    }
}
