package com.simba.elasticjob.handler.sharding.impl;

import com.simba.elasticjob.handler.sharding.JobInstance;
import com.simba.elasticjob.handler.sharding.JobShardingStrategy;

import java.util.*;

/**
 * @Description  【基于平均分配算法的分片策略】
 *          如果作业服务器数量和分片数不能整除，剩余不能被整除部分的分片项将会被依次添加到序号较小的服务器上
 * 例如：
 *      -1. 如果有3台作业服务器，作业分片总数是9，则每一台作业运行服务器上分配的作业分片为：1[0,1,2], 2[3,4,5], 3[6,7,8]
 *      -2. 如果有3台作业服务器，作业分片总数是8，则每一台作业运行服务器上分配的作业分片为：1[0,1,6], 2[2,3,7], 3[4,5]
 *      -3. 如果有3台作业服务器，作业分片总数是10，则每一台作业运行服务器上分配的作业分片为：1[0,1,2,9], 2[3,4,5], 3[6,7,8]
 *
 * @Author yuanjx3
 * @Date 2021/1/18 17:49
 * @Version V1.0
 **/
public class AverageAllocationJobShardingStrategy implements JobShardingStrategy {
    @Override
    public Map<JobInstance, List<Integer>> sharding(List<JobInstance> jobInstances, String jobName, int shardingTotalCount) {
        if (jobInstances.isEmpty()){
            return Collections.emptyMap();
        }
        Map<JobInstance, List<Integer>> result = shardingAliquot(jobInstances,shardingTotalCount);
        addAliquant(jobInstances, shardingTotalCount, result);
        return result;
    }

    /** 功能描述: 对可以整除的部分进行分片
    * @Author: yuanjx3
    * @Date: 2021/1/18 18:51
    */
    private Map<JobInstance, List<Integer>> shardingAliquot(List<JobInstance> shardingUnits, int shardingTotalCount) {
        Map<JobInstance,List<Integer>> result = new LinkedHashMap<>(shardingUnits.size(), 1);
        // 计算出整除部分
        int itemCountPerSharding = shardingTotalCount  / shardingUnits.size();
        int count = 0;
        for (JobInstance each:shardingUnits){
            List<Integer> shardingItems = new ArrayList<>(itemCountPerSharding + 1);
            for (int i=count*itemCountPerSharding; i<(count+1)*itemCountPerSharding; i++){
                shardingItems.add(i);
            }
            result.put(each, shardingItems);
            count++;
        }
        return result;
    }

    /** 功能描述:  添加除余部分的分片
    * @Author: yuanjx3
    * @Date: 2021/1/18 18:52
    */
    private void addAliquant(List<JobInstance> shardingUnits, int shardingTotalCount, Map<JobInstance, List<Integer>> shardingResults) {
        // 计算出余数部分
        int aliquant = shardingTotalCount % shardingUnits.size();
        int count = 0;
        // 循环遍历前面整除部分分片结果集合
        for (Map.Entry<JobInstance, List<Integer>> entry:shardingResults.entrySet()){
            // 分片是从0开始编号的，所以count最大值为 aliquant - 1
            if (count<aliquant){
                // 往每一个server的分片队列里加一个除余分片，直至除余部分的分片全部添加完
                entry.getValue().add(shardingTotalCount / shardingUnits.size() * shardingUnits.size() + count);
            }
            count++;
        }

    }

    @Override
    public String getType() {
        return "AVG_ALLOCATION";
    }
}
