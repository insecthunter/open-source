package com.simba.elasticjob.executor.item.simple;

import com.simba.elasticjob.api.ShardingContext;
import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.executor.JobFacade;
import com.simba.elasticjob.executor.item.impl.ClassedJobItemExecutor;

/**
 * @Description Simple job executor 简单作业执行器
 * @Author yuanjx3
 * @Date 2021/1/22 11:11
 * @Version V1.0
 **/
public class SimpleJobExecutor implements ClassedJobItemExecutor<SimpleJob> {
    @Override
    public void process(final SimpleJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final ShardingContext shardingContext) {
        elasticJob.execute(shardingContext);
    }

    @Override
    public Class<SimpleJob> getElasticJobClass() {
        return SimpleJob.class;
    }
}
