package com.simba.elasticjob.executor;

import com.simba.elasticjob.configuration.JobConfiguration;

/**
 * @Description Reloadable Post Processor
 * @Author yuanjx3
 * @Date 2021/1/22 8:41
 * @Version V1.0
 **/
public interface ReloadablePostProcessor {
    /**
     * Initialize reloadable.
     *
     * @param jobConfig job configuration
     */
    void init(JobConfiguration jobConfig);
}
