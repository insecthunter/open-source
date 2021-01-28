package com.simba.elasticjob.internal.setup;


import com.simba.elasticjob.api.ElasticJob;

/**
 * Job class name provider.
 */
public interface JobClassNameProvider {
    
    /**
     * Get job class name.
     *
     * @param elasticJob job instance
     * @return job class name
     */
    String getJobClassName(ElasticJob elasticJob);
}
