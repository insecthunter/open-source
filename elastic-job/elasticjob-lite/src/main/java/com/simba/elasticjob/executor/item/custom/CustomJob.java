package com.simba.elasticjob.executor.item.custom;

import com.simba.elasticjob.api.ElasticJob;
import com.simba.elasticjob.api.ShardingContext;

/**
 * @Description 用户自定义作业
 * @Author yuanjx3
 * @Date 2021/1/22 14:22
 * @Version V1.0
 **/
public interface CustomJob extends ElasticJob {
    /**
     * Execute custom job.
     *
     * @param shardingContext sharding context
     */
    void execute(ShardingContext shardingContext);
}
