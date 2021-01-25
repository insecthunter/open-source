package com.simba.elasticjob.executor.item;

import com.simba.elasticjob.api.ElasticJob;
import com.simba.elasticjob.exception.JobConfigurationException;
import com.simba.elasticjob.executor.item.impl.ClassedJobItemExecutor;
import com.simba.elasticjob.executor.item.impl.TypedJobItemExecutor;
import com.simba.elasticjob.spi.ElasticJobServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @Description 作业分片项执行器工厂类
 * @Author yuanjx3
 * @Date 2021/1/22 9:06
 * @Version V1.0
 **/
public class JobItemExecutorFactory {
    private static final Logger log = LoggerFactory.getLogger(JobItemExecutorFactory.class);

    private JobItemExecutorFactory(){}

    private static final Map<Class, ClassedJobItemExecutor> CLASSED_EXECUTORS = new HashMap<>();

    static {
        ElasticJobServiceLoader.registerTypedService(TypedJobItemExecutor.class);
        ServiceLoader.load(ClassedJobItemExecutor.class).forEach(each -> {
            log.debug("~~~initing JobItemExecutor :" + each);
            CLASSED_EXECUTORS.put(each.getElasticJobClass(), each);
        });
    }

    /**
     * Get executor.
     * @param elasticJobClass elastic job class
     * @return job item executor
     */
    public static JobItemExecutor getExecutor(final Class<? extends ElasticJob> elasticJobClass) {
        for (Map.Entry<Class, ClassedJobItemExecutor> entry : CLASSED_EXECUTORS.entrySet()) {
            if (entry.getKey().isAssignableFrom(elasticJobClass)) {
                return entry.getValue();
            }
        }
        throw new JobConfigurationException("Can not find executor for elastic job class `%s`", elasticJobClass.getName());
    }

    /**
     * Get executor.
     * @param elasticJobType elastic job type
     * @return job item executor
     */
    public static JobItemExecutor getExecutor(final String elasticJobType) {
        return ElasticJobServiceLoader.getCachedTypedServiceInstance(TypedJobItemExecutor.class, elasticJobType)
                .orElseThrow(() -> new JobConfigurationException("Cannot find executor for elastic job type `%s`", elasticJobType));
    }
}
