package com.simba.elasticjob.handler.sharding;

import com.simba.elasticjob.spi.TypedSPI;

import java.util.List;
import java.util.Map;

/**
 * @Description 作业分片策略接口类
 * @Author yuanjx3
 * @Date 2021/1/18 16:49
 * @Version V1.0
 **/
public interface JobShardingStrategy extends TypedSPI {

    /** 功能描述:   作业分片
    * @Author: yuanjx3
    * @Date: 2021/1/18 16:53
    */
    Map<JobInstance, List<Integer>> sharding(List<JobInstance> jobInstances, String jobName,int shardingTotalCount);
}
