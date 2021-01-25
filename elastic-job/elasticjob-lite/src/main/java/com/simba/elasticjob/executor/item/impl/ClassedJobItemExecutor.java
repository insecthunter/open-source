package com.simba.elasticjob.executor.item.impl;

import com.simba.elasticjob.api.ElasticJob;
import com.simba.elasticjob.executor.item.JobItemExecutor;

/**
 * Classed job item executor. 以Class 类标记类型的作业分片项执行器接口
 * 
 * @param <T> type of ElasticJob
 */
public interface ClassedJobItemExecutor<T extends ElasticJob> extends JobItemExecutor<T> {
    
    /**
     * Get elastic job class.
     * 
     * @return elastic job class
     */
    Class<T> getElasticJobClass();
}
