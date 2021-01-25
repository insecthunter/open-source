package com.simba.elasticjob.handler.sharding;

import com.simba.elasticjob.exception.JobConfigurationException;
import com.simba.elasticjob.spi.ElasticJobServiceLoader;
import com.simba.elasticjob.utils.StringUtils;

/**
 * @Description 作业分片策略工厂类
 * @Author yuanjx3
 * @Date 2021/1/18 16:09
 * @Version V1.0
 **/
public final class JobShardingStrategyFactory {
    private static final String DEFAULT_STRATEGY = "AVG_ALLOCATION";

    private JobShardingStrategyFactory() {}

    static{
        ElasticJobServiceLoader.registerTypedService(JobShardingStrategy.class);
    }
    
    /** 功能描述: 获取作业分片策略
    * @Author: yuanjx3
    * @Date: 2021/1/18 16:13
    */
    public static JobShardingStrategy getStrategy(String type) {
        if (StringUtils.isNullOrEmpty(type)){
            return ElasticJobServiceLoader.getCachedTypedServiceInstance(JobShardingStrategy.class, DEFAULT_STRATEGY).get();
        }
        return ElasticJobServiceLoader.getCachedTypedServiceInstance(JobShardingStrategy.class, type)
                .orElseThrow(()->new JobConfigurationException("Cannot find sharding strategy using type '%s'.", type));
    }
}
