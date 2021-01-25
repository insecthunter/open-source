package com.simba.elasticjob.executor.item.foo;

import com.simba.elasticjob.api.ShardingContext;
import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.executor.JobFacade;
import com.simba.elasticjob.executor.item.impl.ClassedJobItemExecutor;

/**
 * @Description 傻瓜作业分片执行器
 * @Author yuanjx3
 * @Date 2021/1/22 11:18
 * @Version V1.0
 **/
public class ClassedFooJobExecutor implements ClassedJobItemExecutor<FooJob> {
    @Override
    public void process(final FooJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final ShardingContext shardingContext) {
        elasticJob.foo(shardingContext);
    }

    @Override
    public Class<FooJob> getElasticJobClass() {
        return FooJob.class;
    }
}
