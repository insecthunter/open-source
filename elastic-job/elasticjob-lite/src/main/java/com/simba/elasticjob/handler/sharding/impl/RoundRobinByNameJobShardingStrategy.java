package com.simba.elasticjob.handler.sharding.impl;

import com.simba.elasticjob.handler.sharding.JobInstance;
import com.simba.elasticjob.handler.sharding.JobShardingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description  【根据作业名轮询的分片策略】
 * @Author yuanjx3
 * @Date 2021/1/18 19:28
 * @Version V1.0
 **/
public class RoundRobinByNameJobShardingStrategy implements JobShardingStrategy {
    private final AverageAllocationJobShardingStrategy averageAllocationJobShardingStrategy = new AverageAllocationJobShardingStrategy();

    @Override
    public Map<JobInstance, List<Integer>> sharding(List<JobInstance> jobInstances, String jobName, int shardingTotalCount) {
        return averageAllocationJobShardingStrategy.sharding(rotateServerList(jobInstances,jobName), jobName, shardingTotalCount);
    }

    private List<JobInstance> rotateServerList(List<JobInstance> shardingUnits, String jobName) {
        // 得到作业服务器数量
        int shardingUnitsSize = shardingUnits.size();
        // 计算作业名的hash码与服务器数量取余的绝对值，记作服务器队里的偏置量
        int offset = Math.abs(jobName.hashCode()) % shardingUnitsSize;
        // 结果为0 ，直接返回原服务器队列
        if (0 == offset){
            return shardingUnits;
        }
        /**
        * 计算新的服务器队里中服务器的顺序
         * 对面计算得到的偏置量进行累加，每累加一次，计算它与服务器数量的余数，记为新的服务器队列中的下一个服务器
        */

        List<JobInstance> result = new ArrayList<>(shardingUnitsSize);
        for (int i=0; i<shardingUnitsSize; i++){
            int index = (1+offset) % shardingUnitsSize;
            result.add(shardingUnits.get(index));
        }
        return result;
    }

    @Override
    public String getType() {
        return "ROUND_ROBIN";
    }
}
