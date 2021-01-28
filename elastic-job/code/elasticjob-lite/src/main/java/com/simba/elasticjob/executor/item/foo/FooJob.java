package com.simba.elasticjob.executor.item.foo;

import com.simba.elasticjob.api.ElasticJob;
import com.simba.elasticjob.api.ShardingContext;

/**
 * @Description 傻瓜式作业
 * @Author yuanjx3
 * @Date 2021/1/22 11:17
 * @Version V1.0
 **/
public interface FooJob extends ElasticJob {

    /**
     * Do job.
     * @param shardingContext sharding context
     */
    void foo(ShardingContext shardingContext);
}
