package com.simba.elasticjob.executor.item.dataflow;

import com.simba.elasticjob.api.ElasticJob;
import com.simba.elasticjob.api.ShardingContext;

import java.util.List;

/**
 * @Description Dataflow job. 数据流作业
 *
 * @param <T> type of data
 * @Author yuanjx3
 * @Date 2021/1/22 11:25
 * @Version V1.0
 **/
public interface DataflowJob<T> extends ElasticJob {
    /**
     * Fetch to be processed data.
     *
     * @param shardingContext sharding context
     * @return to be processed data
     */
    List<T> fetchData(ShardingContext shardingContext);

    /**
     * Process data.
     *
     * @param shardingContext sharding context
     * @param data to be processed data
     */
    void processData(ShardingContext shardingContext, List<T> data);
}
