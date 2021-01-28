package com.simba.elasticjob.executor.item;

import com.simba.elasticjob.api.ElasticJob;
import com.simba.elasticjob.api.ShardingContext;
import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.executor.JobFacade;

/**
 * @Description Job item executor.
 *
 * @param <T> type of ElasticJob
 * @Author yuanjx3
 * @Date 2021/1/22 8:34
 * @Version V1.0
 **/
public interface JobItemExecutor<T extends ElasticJob> {

    /**
     * Process job item.
     *
     * @param elasticJob elastic job
     * @param jobConfig job configuration
     * @param jobFacade job facade
     * @param shardingContext sharding context
     */
    void process(T elasticJob, JobConfiguration jobConfig, JobFacade jobFacade, ShardingContext shardingContext);
}
