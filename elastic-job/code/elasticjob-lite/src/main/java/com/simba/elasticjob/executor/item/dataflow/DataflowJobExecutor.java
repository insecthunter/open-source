package com.simba.elasticjob.executor.item.dataflow;

import com.simba.elasticjob.api.ShardingContext;
import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.executor.JobFacade;
import com.simba.elasticjob.executor.item.impl.ClassedJobItemExecutor;

import java.util.List;

/**
 * @Description 数据流作业分片项执行器
 * @Author yuanjx3
 * @Date 2021/1/22 11:25
 * @Version V1.0
 **/
public class DataflowJobExecutor implements ClassedJobItemExecutor<DataflowJob> {

    @Override
    public void process(final DataflowJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final ShardingContext shardingContext) {
        if (Boolean.parseBoolean(jobConfig.getProps().getOrDefault(DataflowJobProperties.STREAM_PROCESS_KEY, false).toString())) {
            streamingExecute(elasticJob, jobConfig, jobFacade, shardingContext);
        } else {
            oneOffExecute(elasticJob, shardingContext);
        }
    }

    private void streamingExecute(final DataflowJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final ShardingContext shardingContext) {
        List<Object> data = fetchData(elasticJob, shardingContext);
        while (null != data && !data.isEmpty()) {
            processData(elasticJob, shardingContext, data);
            if (!isEligibleForJobRunning(jobConfig, jobFacade)) {
                break;
            }
            data = fetchData(elasticJob, shardingContext);
        }
    }

    private boolean isEligibleForJobRunning(final JobConfiguration jobConfig, final JobFacade jobFacade) {
        return !jobFacade.isNeedSharding() && Boolean.parseBoolean(jobConfig.getProps().getOrDefault(DataflowJobProperties.STREAM_PROCESS_KEY, false).toString());
    }

    private void oneOffExecute(final DataflowJob elasticJob, final ShardingContext shardingContext) {
        List<Object> data = fetchData(elasticJob, shardingContext);
        if (null != data && !data.isEmpty()) {
            processData(elasticJob, shardingContext, data);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> fetchData(final DataflowJob elasticJob, final ShardingContext shardingContext) {
        return elasticJob.fetchData(shardingContext);
    }

    @SuppressWarnings("unchecked")
    private void processData(final DataflowJob elasticJob, final ShardingContext shardingContext, final List<Object> data) {
        elasticJob.processData(shardingContext, data);
    }

    @Override
    public Class<DataflowJob> getElasticJobClass() {
        return DataflowJob.class;
    }
}
