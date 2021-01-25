package com.simba.elasticjob.handler.threadpool;

import com.simba.elasticjob.exception.JobConfigurationException;
import com.simba.elasticjob.spi.ElasticJobServiceLoader;
import com.simba.elasticjob.utils.StringUtils;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/22 8:56
 * @Version V1.0
 **/
public class JobExecutorServiceHandlerFactory {
    public static final String DEFAULT_HANDLER = "CPU";

    static {
        ElasticJobServiceLoader.registerTypedService(JobExecutorServiceHandler.class);
    }

    /**
     * Get job executor service handler.
     *
     * @param type executor service handler type
     * @return executor service handler
     */
    public static JobExecutorServiceHandler getHandler(final String type) {
        if (StringUtils.isNullOrEmpty(type)) {
            return ElasticJobServiceLoader.getCachedTypedServiceInstance(JobExecutorServiceHandler.class, DEFAULT_HANDLER).get();
        }
        return ElasticJobServiceLoader.getCachedTypedServiceInstance(JobExecutorServiceHandler.class, type)
                .orElseThrow(() -> new JobConfigurationException("Cannot find executor service handler using type '%s'.", type));
    }
}
