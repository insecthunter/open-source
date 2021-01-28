package com.simba.elasticjob.handler.threadpool;

import com.simba.elasticjob.exception.JobConfigurationException;
import com.simba.elasticjob.spi.ElasticJobServiceLoader;
import com.simba.elasticjob.utils.StringUtils;

/**
 * @Description 作业执行服务处理器工厂类
 * @Author yuanjx3
 * @Date 2021/1/22 8:56
 * @Version V1.0
 **/
public class JobExecutorServiceHandlerFactory {
    public static final String DEFAULT_HANDLER = "CPU";

    static {
        // 加载所有的作业执行服务处理器接口的实现类
        ElasticJobServiceLoader.registerTypedService(JobExecutorServiceHandler.class);
    }

    /**
     * Get job executor service handler.
     * 默认返回：CPUUsageJobExecutorServiceHandler
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
