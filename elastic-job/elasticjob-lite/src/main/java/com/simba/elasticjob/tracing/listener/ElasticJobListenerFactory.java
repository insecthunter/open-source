package com.simba.elasticjob.tracing.listener;

import com.simba.elasticjob.internal.listener.ElasticJobListener;
import com.simba.elasticjob.spi.ElasticJobServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;

/**
 * @Description ElasticJob 监听器工程类
 * @Author yuanjx3
 * @Date 2021/1/22 9:41
 * @Version V1.0
 **/
public class ElasticJobListenerFactory {
    private static final Logger log = LoggerFactory.getLogger(ElasticJobListenerFactory.class);
    private ElasticJobListenerFactory(){}
    static {
        ElasticJobServiceLoader.registerTypedService(ElasticJobListener.class);
    }

    /**
     * Create a job listener instance.
     *
     * @param type job listener type
     * @return optional job listener instance
     */
    public static Optional<ElasticJobListener> createListener(final String type) {
        log.debug("ElasticJob监听器工程类创建监听器： 【" + type + "】");
        return ElasticJobServiceLoader.newTypedServiceInstance(ElasticJobListener.class, type, new Properties());
    }
}
