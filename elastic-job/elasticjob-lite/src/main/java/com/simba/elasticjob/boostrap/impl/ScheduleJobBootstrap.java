package com.simba.elasticjob.boostrap.impl;

import com.simba.elasticjob.api.ElasticJob;
import com.simba.elasticjob.boostrap.JobBootstrap;
import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.internal.schedule.JobScheduler;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import com.simba.elasticjob.utils.Preconditions;
import com.simba.elasticjob.utils.StringUtils;

/**
 * @Description 周期性调度作业启动类
 * @Author yuanjx3
 * @Date 2021/1/13 20:20
 * @Version V1.0
 **/
public class ScheduleJobBootstrap implements JobBootstrap {
    private  final JobScheduler jobScheduler;

    public ScheduleJobBootstrap(CoordinatorRegistryCenter registryCenter, ElasticJob elasticJob, JobConfiguration jobConfig){
        jobScheduler = new JobScheduler(registryCenter, elasticJob, jobConfig);
    }

    public ScheduleJobBootstrap(CoordinatorRegistryCenter registryCenter, String elasticJobType, JobConfiguration jobConfig){
        jobScheduler = new JobScheduler(registryCenter, elasticJobType, jobConfig);
    }

    /** 作业调度 **/
    public void schedule(){
        Preconditions.checkArgument(!StringUtils.isNullOrEmpty(jobScheduler.getJobConfig().getCron()),"Cron can not be empty.");
        jobScheduler.getJobSchedulerController().shedulerJob(jobScheduler.getJobConfig().getCron());
    }
    @Override
    public void shutdown() {
        jobScheduler.shutdown();
    }
}
