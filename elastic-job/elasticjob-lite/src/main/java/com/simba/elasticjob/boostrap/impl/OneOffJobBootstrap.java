package com.simba.elasticjob.boostrap.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.simba.elasticjob.api.ElasticJob;
import com.simba.elasticjob.boostrap.JobBootstrap;
import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.internal.instance.InstanceService;
import com.simba.elasticjob.internal.schedule.JobScheduler;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

/**
 * @Description 一次性作业启动类
 * @Author yuanjx3
 * @Date 2021/1/21 12:43
 * @Version V1.0
 **/
public final class OneOffJobBootstrap implements JobBootstrap {
    private  final JobScheduler jobScheduler;
    private final InstanceService instanceService;

    public OneOffJobBootstrap(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig) {
        Preconditions.checkArgument(Strings.isNullOrEmpty(jobConfig.getCron()), "Cron should be empty.");
        jobScheduler = new JobScheduler(regCenter, elasticJob, jobConfig);
        instanceService = new InstanceService(regCenter, jobConfig.getJobName());
    }

    public OneOffJobBootstrap(final CoordinatorRegistryCenter regCenter, final String elasticJobType, final JobConfiguration jobConfig) {
        Preconditions.checkArgument(Strings.isNullOrEmpty(jobConfig.getCron()), "Cron should be empty.");
        jobScheduler = new JobScheduler(regCenter, elasticJobType, jobConfig);
        instanceService = new InstanceService(regCenter, jobConfig.getJobName());
    }

    /**
     * Execute job.
     */
    public void execute() {
        instanceService.triggerAllInstances();
    }

    @Override
    public void shutdown() {
        jobScheduler.shutdown();
    }
}
