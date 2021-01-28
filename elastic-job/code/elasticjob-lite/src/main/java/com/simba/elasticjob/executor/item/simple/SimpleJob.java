package com.simba.elasticjob.executor.item.simple;

import com.simba.elasticjob.api.ElasticJob;
import com.simba.elasticjob.api.ShardingContext;

/**
 * @Description 简单作业
 * @Author yuanjx3
 * @Date 2021/1/22 10:49
 * @Version V1.0
 **/
public interface SimpleJob extends ElasticJob {
    /**
     * Execute job.
     *
     * @param shardingContext sharding context
     */
    void execute(ShardingContext shardingContext);
}
