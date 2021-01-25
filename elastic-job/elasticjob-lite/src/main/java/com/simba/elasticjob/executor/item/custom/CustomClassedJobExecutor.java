package com.simba.elasticjob.executor.item.custom;

import com.simba.elasticjob.api.ShardingContext;
import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.executor.JobFacade;
import com.simba.elasticjob.executor.item.impl.ClassedJobItemExecutor;

/**
 * @Description 用户自定义作业分片执行器
 * @Author yuanjx3
 * @Date 2021/1/22 14:20
 * @Version V1.0
 **/
public class CustomClassedJobExecutor implements ClassedJobItemExecutor<CustomJob> {
    @Override
    public void process(final CustomJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final ShardingContext shardingContext) {
        elasticJob.execute(shardingContext);
    }

    @Override
    public Class<CustomJob> getElasticJobClass() {
        return CustomJob.class;
    }
}
