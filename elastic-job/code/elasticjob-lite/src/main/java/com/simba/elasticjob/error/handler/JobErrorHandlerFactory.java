package com.simba.elasticjob.error.handler;

import com.simba.elasticjob.spi.ElasticJobServiceLoader;
import com.simba.elasticjob.utils.StringUtils;

import java.util.Optional;
import java.util.Properties;

/**
 * @Description  Job error handler factory.
 * @Author yuanjx3
 * @Date 2021/1/22 8:49
 * @Version V1.0
 **/
public final class JobErrorHandlerFactory {
    public static final String DEFAULT_HANDLER = "LOG";

    private JobErrorHandlerFactory(){}

    static {
        ElasticJobServiceLoader.registerTypedService(JobErrorHandler.class);
    }

    /**
     * Get job error handler.
     *
     * @param type job error handler type
     * @param props job properties
     * @return job error handler
     */
    public static Optional<JobErrorHandler> createHandler(final String type, final Properties props) {
        if (StringUtils.isNullOrEmpty(type)) {
            return ElasticJobServiceLoader.newTypedServiceInstance(JobErrorHandler.class, DEFAULT_HANDLER, props);
        }
        return ElasticJobServiceLoader.newTypedServiceInstance(JobErrorHandler.class, type, props);
    }
}
