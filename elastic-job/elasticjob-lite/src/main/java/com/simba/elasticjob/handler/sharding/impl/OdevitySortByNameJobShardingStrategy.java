package com.simba.elasticjob.handler.sharding.impl;

import com.simba.elasticjob.handler.sharding.JobInstance;
import com.simba.elasticjob.handler.sharding.JobShardingStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Description 【通过对作业名进行hash计算来确定IP asc或desc的分片策略】
 *          如果作业名的 hashcode 是奇数，则采用 IP asc(升序)的分片策略；
 *          如果作业名的 hashcode 是偶数，则采用 IP desc(降序)的分片策略；
 *          用于对作业服务器进行平均分配
 *
 * 例如：
 *      1.如果有3台作业执行服务器和2个作业分片，而且作业名的hash值是奇数，则每台服务器将被划分为：1 = [0], 2 = [1], 3 = [];
 *      2.如果有3台作业执行服务器和2个作业分片，而且作业名的hash值是偶数，则每台服务器将被划分为：3 = [0], 2 = [1], 1 = [];
 * @Author yuanjx3
 * @Date 2021/1/18 19:10
 * @Version V1.0
 **/
public class OdevitySortByNameJobShardingStrategy implements JobShardingStrategy {
    private final AverageAllocationJobShardingStrategy averageAllocationJobShardingStrategy = new AverageAllocationJobShardingStrategy();

    @Override
    public Map<JobInstance, List<Integer>> sharding(List<JobInstance> jobInstances, String jobName, int shardingTotalCount) {
        long jobNameHash = jobName.hashCode();
        if (0 == jobNameHash % 2){
            // 如果任务名的hash值是偶数，就将服务器集合进行翻转，这样后面反向迭代服务器集合，
            // 往每一台server上添加作业分片，依次达到IP DESC策略的效果
            Collections.reverse(jobInstances);
        }
        return averageAllocationJobShardingStrategy.sharding(jobInstances, jobName, shardingTotalCount);
    }

    @Override
    public String getType() {
        return "ODEVITY";
    }
}
