package com.simba.elasticjob.internal.listener;

import com.simba.elasticjob.spi.TypedSPI;

/**
 * @Description ElasticJob侦听器
 * @Author yuanjx3
 * @Date 2021/1/19 17:18
 * @Version V1.0
 **/
public interface ElasticJobListener extends TypedSPI {

    /** 功能描述: 在作业执行之前调用
    * @param: [shardingContexts]
    * @Author: yuanjx3
    * @Date: 2021/1/19 17:19
    */
    void beforeJobExecuted(ShardingContexts shardingContexts);

    /** 功能描述: 在作业执行之后调用
     * @param: [shardingContexts]
     * @Author: yuanjx3
     * @Date: 2021/1/19 17:19
     */
    void afterJobExecuted(ShardingContexts shardingContexts);
}
