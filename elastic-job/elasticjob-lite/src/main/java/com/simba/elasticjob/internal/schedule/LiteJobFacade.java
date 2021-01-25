package com.simba.elasticjob.internal.schedule;

import com.google.common.base.Strings;
import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.context.TaskContext;
import com.simba.elasticjob.event.JobExecutionEvent;
import com.simba.elasticjob.event.JobStatusTraceEvent;
import com.simba.elasticjob.event.JobStatusTraceEvent.Source;
import com.simba.elasticjob.event.JobStatusTraceEvent.State;
import com.simba.elasticjob.exception.JobExecutionEnvironmentException;
import com.simba.elasticjob.executor.JobFacade;
import com.simba.elasticjob.internal.config.ConfigurationService;
import com.simba.elasticjob.internal.failover.FailoverService;
import com.simba.elasticjob.internal.listener.ElasticJobListener;
import com.simba.elasticjob.internal.listener.ShardingContexts;
import com.simba.elasticjob.internal.sharding.ExecutionContextService;
import com.simba.elasticjob.internal.sharding.ExecutionService;
import com.simba.elasticjob.internal.sharding.ShardingService;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import com.simba.elasticjob.tracing.JobTracingEventBus;
import com.simba.elasticjob.tracing.api.TracingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/21 16:11
 * @Version V1.0
 **/
public final class LiteJobFacade implements JobFacade {
    private final  Logger log = LoggerFactory.getLogger(getClass());

    private final ConfigurationService configService;
    private final ShardingService shardingService;
    private final ExecutionContextService executionContextService;
    private final ExecutionService executionService;
    private final FailoverService failoverService;
    private final Collection<ElasticJobListener> elasticJobListeners;
    private final JobTracingEventBus jobTracingEventBus;

    public LiteJobFacade(final CoordinatorRegistryCenter regCenter, final String jobName, final Collection<ElasticJobListener> elasticJobListeners, final TracingConfiguration<?> tracingConfig) {
        configService = new ConfigurationService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        executionContextService = new ExecutionContextService(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
        failoverService = new FailoverService(regCenter, jobName);
        this.elasticJobListeners = elasticJobListeners;
        this.jobTracingEventBus = null == tracingConfig ? new JobTracingEventBus() : new JobTracingEventBus(tracingConfig);
    }

    @Override
    public JobConfiguration loadJobConfiguration(final boolean fromCache) {
        return configService.load(fromCache);
    }

    @Override
    public void checkJobExecutionEnvironment() throws JobExecutionEnvironmentException {
        configService.checkMaxTimeDiffSecondsTolerable();
    }

    @Override
    public void failoverIfNecessary() {
        if (configService.load(true).isFailover()) {
            failoverService.failoverIfNecessary();
        }
    }

    @Override
    public void registerJobBegin(final ShardingContexts shardingContexts) {
        executionService.registerJobBegin(shardingContexts);
    }

    @Override
    public void registerJobCompleted(final ShardingContexts shardingContexts) {
        executionService.registerJobCompleted(shardingContexts);
        if (configService.load(true).isFailover()) {
            failoverService.updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
        }
    }

    @Override
    public ShardingContexts getShardingContexts() {
        boolean isFailover = configService.load(true).isFailover();
        if (isFailover) {
            List<Integer> failoverShardingItems = failoverService.getLocalFailoverItems();
            if (!failoverShardingItems.isEmpty()) {
                return executionContextService.getJobShardingContext(failoverShardingItems);
            }
        }
        shardingService.shardingIfNecessary();
        List<Integer> shardingItems = shardingService.getLocalShardingItems();
        if (isFailover) {
            shardingItems.removeAll(failoverService.getLocalTakeOffItems());
        }
        shardingItems.removeAll(executionService.getDisabledItems(shardingItems));
        return executionContextService.getJobShardingContext(shardingItems);
    }

    @Override
    public boolean misfireIfRunning(final Collection<Integer> shardingItems) {
        return executionService.misfireIfHasRunningItems(shardingItems);
    }

    @Override
    public void clearMisfire(final Collection<Integer> shardingItems) {
        executionService.clearMisfire(shardingItems);
    }

    @Override
    public boolean isExecuteMisfired(final Collection<Integer> shardingItems) {
        return !isNeedSharding() && configService.load(true).isMisfire() && !executionService.getMisfiredJobItems(shardingItems).isEmpty();
    }

    @Override
    public boolean isNeedSharding() {
        return shardingService.isNeedSharding();
    }

    @Override
    public void beforeJobExecuted(final ShardingContexts shardingContexts) {
        for (ElasticJobListener each : elasticJobListeners) {
            each.beforeJobExecuted(shardingContexts);
        }
    }

    @Override
    public void afterJobExecuted(final ShardingContexts shardingContexts) {
        for (ElasticJobListener each : elasticJobListeners) {
            each.afterJobExecuted(shardingContexts);
        }
    }

    @Override
    public void postJobExecutionEvent(final JobExecutionEvent jobExecutionEvent) {
        jobTracingEventBus.post(jobExecutionEvent);
    }

    @Override
    public void postJobStatusTraceEvent(final String taskId, final State state, final String message) {
        TaskContext taskContext = TaskContext.from(taskId);
        jobTracingEventBus.post(new JobStatusTraceEvent(taskContext.getMetaInfo().getJobName(), taskContext.getId(),
                taskContext.getSlaveId(), Source.LITE_EXECUTOR, taskContext.getType().name(), taskContext.getMetaInfo().getShardingItems().toString(), state, message));
        if (!Strings.isNullOrEmpty(message)) {
            log.trace(message);
        }
    }
}
